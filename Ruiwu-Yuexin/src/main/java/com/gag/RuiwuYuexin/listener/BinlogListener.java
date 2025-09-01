package com.gag.RuiwuYuexin.listener;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.gag.RuiwuYuexin.config.RabbitConfig.BINLOG_EXCHANGE;
import static com.gag.RuiwuYuexin.config.RabbitConfig.ROUTING_KEY_PREFIX;

/**
 * @author : 葛澳港
 * @description : MySQL Binlog监听器，用于捕获数据库变更事件并发送到RabbitMQ
 * @createDate : 2025-09-01 8:30
 */

@Component
public class BinlogListener {

    private static final Logger logger = LoggerFactory.getLogger(BinlogListener.class);

    private final RabbitTemplate rabbitTemplate;
    private BinaryLogClient client;
    private ExecutorService executorService;
    private volatile boolean running = false;

    // 从配置文件中读取MySQL连接信息
    @Value("${mysql.host:localhost}")
    private String mysqlHost;

    @Value("${mysql.port:3306}")
    private int mysqlPort;

    @Value("${mysql.username:root}")
    private String mysqlUsername;

    @Value("${mysql.password:root}")
    private String mysqlPassword;

    @Value("${mysql.binlog.server-id:123456}")
    private long serverId;

    // 要监听的数据库
    @Value("${mysql.binlog.database:}")
    private String targetDatabase;

    // 要监听的表，格式：database.table
    @Value("#{T(java.util.Collections).emptyList()}")
    private List<String> targetTables;
    
    @Value("#{${mysql.binlog.tables: T(java.util.Collections).emptyList()}}")
    public void setTargetTables(List<String> tables) {
        if (tables != null && !tables.isEmpty()) {
            this.targetTables = tables;
        }
    }

    // 存储要监听的表的集合
    private final Set<String> watchedTables = new HashSet<>();
    
    // 存储表ID到表信息的映射
    private final Map<Long, TableInfo> tableInfoMap = new HashMap<>();
    
    // 重连间隔（毫秒）
    private static final long RECONNECT_INTERVAL_MS = 5000;
    // 最大重连次数
    private static final int MAX_RECONNECT_ATTEMPTS = 5;

    public BinlogListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostConstruct
    public void start() {
        logger.info("开始初始化Binlog监听器");
        
        // 初始化监听表集合
        if (targetTables != null && !targetTables.isEmpty()) {
            watchedTables.addAll(targetTables);
            logger.info("已配置监听表: {}", String.join(", ", watchedTables));
        }
        
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "binlog-listener-thread");
            thread.setDaemon(true);
            return thread;
        });

        running = true;
        executorService.execute(this::connectWithRetry);
    }

    /**
     * 带重试机制的连接方法
     */
    private void connectWithRetry() {
        int attempt = 0;
        while (running && attempt < MAX_RECONNECT_ATTEMPTS) {
            try {
                if (attempt > 0) {
                    logger.info("第{}次尝试重连MySQL Binlog服务...", attempt + 1);
                    TimeUnit.MILLISECONDS.sleep(RECONNECT_INTERVAL_MS);
                }
                
                connect();
                break; // 连接成功后跳出循环
            } catch (Exception e) {
                attempt++;
                logger.error("连接MySQL Binlog服务失败 (尝试 {}/{}): {}", 
                        attempt, MAX_RECONNECT_ATTEMPTS, e.getMessage());
                
                if (attempt >= MAX_RECONNECT_ATTEMPTS) {
                    logger.error("达到最大重连次数，停止重试");
                }
            }
        }
    }

    /**
     * 连接到MySQL Binlog服务
     */
    private void connect() throws Exception {
        client = new BinaryLogClient(mysqlHost, mysqlPort, mysqlUsername, mysqlPassword);
        client.setServerId(serverId);
        
        // 注册事件监听器
        client.registerEventListener(event -> {
            EventType eventType = event.getHeader().getEventType();
            EventData data = event.getData();
            
            // 处理表映射事件
            if (data instanceof TableMapEventData) {
                handleTableMapEvent((TableMapEventData) data);
                return;
            }
            
            // 处理写、更新、删除事件
            if (data instanceof WriteRowsEventData || 
                data instanceof UpdateRowsEventData || 
                data instanceof DeleteRowsEventData) {
                
                // 获取表ID
                long tableId = getTableId(event);
                TableInfo tableInfo = getTableInfo(tableId);
                String tableName = tableInfo != null ? tableInfo.getTableName() : "unknown_table";
                String databaseName = tableInfo != null ? tableInfo.getDatabaseName() : "unknown_database";
                
                // 判断是否需要处理该表的事件
                if (!shouldProcessEvent(databaseName, tableName)) {
                    logger.debug("跳过不监听的表: {}.{}", databaseName, tableName);
                    return;
                }
                
                logger.info("捕获到表 {}.{} 的事件: {}", databaseName, tableName, eventType);
                
                // 根据事件类型处理
                String routingKey = null;
                if (data instanceof WriteRowsEventData) {
                    routingKey = ROUTING_KEY_PREFIX + "insert";
                } else if (data instanceof UpdateRowsEventData) {
                    routingKey = ROUTING_KEY_PREFIX + "update";
                } else if (data instanceof DeleteRowsEventData) {
                    routingKey = ROUTING_KEY_PREFIX + "delete";
                }
                
                // 发送消息到RabbitMQ，包含数据库名和表名信息
                if (routingKey != null) {
                    try {
                        // 构建包含数据库和表信息的消息
                        String message = buildMessageWithMetadata(databaseName, tableName, data.toString());
                        rabbitTemplate.convertAndSend(BINLOG_EXCHANGE, routingKey, message);
                        logger.debug("已发送Binlog事件到RabbitMQ: {}", routingKey);
                    } catch (Exception e) {
                        logger.error("发送Binlog事件到RabbitMQ失败: {}", e.getMessage());
                    }
                }
            }
        });
        
        // 添加连接监听器
        client.registerLifecycleListener(new BinaryLogClient.LifecycleListener() {
            @Override
            public void onConnect(BinaryLogClient client) {
                logger.info("成功连接到MySQL Binlog服务");
            }

            @Override
            public void onCommunicationFailure(BinaryLogClient client, Exception ex) {
                logger.error("与MySQL Binlog服务通信失败: {}", ex.getMessage());
                // 连接失败后尝试重连
                if (running) {
                    executorService.execute(BinlogListener.this::connectWithRetry);
                }
            }

            @Override
            public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) {
                logger.error("Binlog事件反序列化失败: {}", ex.getMessage());
            }

            @Override
            public void onDisconnect(BinaryLogClient client) {
                logger.info("已断开与MySQL Binlog服务的连接");
            }
        });
        
        // 连接到MySQL
        client.connect();
        logger.info("Binlog监听器已成功启动");
    }

    /**
     * 判断是否需要处理该事件
     */
    private boolean shouldProcessEvent(String databaseName, String tableName) {
        // 如果没有配置监听的数据库和表，则处理所有事件
        if ((targetDatabase == null || targetDatabase.isEmpty()) && 
            (watchedTables == null || watchedTables.isEmpty())) {
            return true;
        }
        
        // 如果配置了数据库，则检查数据库是否匹配
        if (targetDatabase != null && !targetDatabase.isEmpty() && 
            !targetDatabase.equalsIgnoreCase(databaseName)) {
            return false;
        }
        
        // 如果配置了监听表，则检查表是否在监听列表中
        if (watchedTables != null && !watchedTables.isEmpty()) {
            // 构建完整的表名格式: database.table
            String fullTableName = databaseName + "." + tableName;
            return watchedTables.contains(fullTableName);
        }
        
        return true;
    }

    /**
     * 构建包含元数据的消息
     */
    private String buildMessageWithMetadata(String databaseName, String tableName, String data) {
        // 简单实现：格式为 database:table:data
        return databaseName + ":" + tableName + ":" + data;
    }

    /**
     * 处理表映射事件，记录表ID和表信息的映射关系
     */
    private void handleTableMapEvent(TableMapEventData tableMapData) {
        long tableId = tableMapData.getTableId();
        String databaseName = tableMapData.getDatabase();
        String tableName = tableMapData.getTable();
        
        // 存储表信息
        tableInfoMap.put(tableId, new TableInfo(databaseName, tableName));
        
        logger.debug("记录表映射关系: tableId={}, database={}, table={}", 
                tableId, databaseName, tableName);
    }
    
    /**
     * 获取表ID
     */
    private long getTableId(Event event) {
        EventData data = event.getData();
        
        // 如果是表映射事件，直接返回表ID
        if (data instanceof TableMapEventData) {
            return ((TableMapEventData) data).getTableId();
        }
        
        // 对于数据变更事件，需要从事件头中获取表ID
        if (data instanceof WriteRowsEventData) {
            return ((WriteRowsEventData) data).getTableId();
        } else if (data instanceof UpdateRowsEventData) {
            return ((UpdateRowsEventData) data).getTableId();
        } else if (data instanceof DeleteRowsEventData) {
            return ((DeleteRowsEventData) data).getTableId();
        }
        
        return -1;
    }
    
    /**
     * 获取表信息
     */
    private TableInfo getTableInfo(long tableId) {
        return tableInfoMap.get(tableId);
    }
    
    /**
     * 表信息内部类
     */
    private static class TableInfo {
        private final String databaseName;
        private final String tableName;
        
        public TableInfo(String databaseName, String tableName) {
            this.databaseName = databaseName;
            this.tableName = tableName;
        }
        
        public String getDatabaseName() {
            return databaseName;
        }
        
        public String getTableName() {
            return tableName;
        }
    }

    @PreDestroy
    public void stop() {
        logger.info("正在停止Binlog监听器");
        running = false;
        
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (IOException e) {
                logger.error("断开Binlog连接失败: {}", e.getMessage());
            }
        }
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("Binlog监听器已停止");
    }
}
