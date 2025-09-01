package com.gag.RuiwuYuexin.utils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.gag.RuiwuYuexin.service.impl.GoodsServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.gag.RuiwuYuexin.config.RabbitConfig.BINLOG_QUEUE;

/**
 * @author : 葛澳港
 * @description : Binlog消息消费者，用于接收并处理RabbitMQ中的binlog事件，并同步到Redis
 * @createDate : 2025-09-01 8:41
 */
@Component
@RabbitListener(queues = BINLOG_QUEUE)
public class BinlogConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BinlogConsumer.class);
    private final StringRedisTemplate redisTemplate;
    private final GoodsServiceImpl goodsService;

    // Redis键前缀，用于区分不同类型的数据
    private static final String REDIS_KEY_PREFIX = "db:binlog:";
    // 默认过期时间（秒），可根据需要调整
    private static final long DEFAULT_EXPIRE_TIME = 86400; // 24小时

    // 存储表名到Redis键的映射，方便批量操作
    private final Map<String, String> tableKeyMap = new HashMap<>();

    public BinlogConsumer(StringRedisTemplate redisTemplate, GoodsServiceImpl goodsService) {
        this.redisTemplate = redisTemplate;
        this.goodsService = goodsService;
        // 初始化表名到Redis键的映射
        initTableKeyMap();
    }

    /**
     * 初始化表名到Redis键的映射
     */
    private void initTableKeyMap() {
        // 示例：根据实际业务需求添加表名和对应的Redis键前缀
        tableKeyMap.put("goods", "product:");
        tableKeyMap.put("user", "user:");
        // 可以根据实际情况添加更多表的映射
    }

    @RabbitHandler
    public void onMessage(String message) {
        try {
            logger.info("收到Binlog消息，开始处理");
            
            // 记录最后处理的事件
            redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + "lastEvent", message);
            
            // 解析消息，提取数据库名、表名和数据
            MessageMetadata metadata = parseMessageMetadata(message);
            if (metadata == null) {
                logger.warn("无法解析消息格式: {}", message);
                return;
            }
            
            String operationType = metadata.getOperationType();
            String tableName = metadata.getTableName();
            String databaseName = metadata.getDatabaseName();
            
            logger.info("解析到数据库: {}, 表名: {}, 操作类型: {}", databaseName, tableName, operationType);
            
            // 根据操作类型执行不同的Redis同步逻辑
            switch (operationType) {
                case "insert":
                    handleInsert(metadata);
                    break;
                case "update":
                    handleUpdate(metadata);
                    break;
                case "delete":
                    handleDelete(metadata);
                    break;
                default:
                    logger.warn("未知的操作类型: {}", operationType);
            }
            
            logger.info("Binlog消息处理完成");
        } catch (Exception e) {
            logger.error("处理Binlog消息失败: {}", e.getMessage(), e);
            // 可以根据需要添加重试机制或死信队列
        }
    }

    /**
     * 处理插入操作
     */
    private void handleInsert(MessageMetadata metadata) {
        try {
            String tableName = metadata.getTableName();
            JSONObject data = parseEventData(metadata.getData());
            String id = extractPrimaryKey(data);
            
            if (id != null) {
                String redisKey = getRedisKey(tableName, id);
                // 将数据存入Redis
                redisTemplate.opsForValue().set(redisKey, data.toJSONString(), DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
                logger.debug("已将插入数据同步到Redis: {}", redisKey);
            }
            
            // 如果是商品表的操作，清除商品分页缓存
            if ("goods".equals(tableName)) {
                goodsService.clearGoodsPageCache();
                logger.debug("已清除商品分页缓存");
            }
        } catch (Exception e) {
            logger.error("处理插入操作失败: {}", e.getMessage());
        }
    }

    /**
     * 处理更新操作
     */
    private void handleUpdate(MessageMetadata metadata) {
        try {
            String tableName = metadata.getTableName();
            JSONObject data = parseEventData(metadata.getData());
            String id = extractPrimaryKey(data);
            
            if (id != null) {
                String redisKey = getRedisKey(tableName, id);
                // 更新Redis中的数据
                redisTemplate.opsForValue().set(redisKey, data.toJSONString(), DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
                logger.debug("已将更新数据同步到Redis: {}", redisKey);
            }
            
            // 如果是商品表的操作，清除商品分页缓存
            if ("goods".equals(tableName)) {
                goodsService.clearGoodsPageCache();
                logger.debug("已清除商品分页缓存");
            }
        } catch (Exception e) {
            logger.error("处理更新操作失败: {}", e.getMessage());
        }
    }

    /**
     * 处理删除操作
     */
    private void handleDelete(MessageMetadata metadata) {
        try {
            String tableName = metadata.getTableName();
            JSONObject data = parseEventData(metadata.getData());
            String id = extractPrimaryKey(data);
            
            if (id != null) {
                String redisKey = getRedisKey(tableName, id);
                // 从Redis中删除数据
                redisTemplate.delete(redisKey);
                logger.debug("已从Redis中删除数据: {}", redisKey);
            }
            
            // 如果是商品表的操作，清除商品分页缓存
            if ("goods".equals(tableName)) {
                goodsService.clearGoodsPageCache();
                logger.debug("已清除商品分页缓存");
            }
        } catch (Exception e) {
            logger.error("处理删除操作失败: {}", e.getMessage());
        }
    }

    /**
     * 解析消息元数据
     * 消息格式: database:table:data
     */
    private MessageMetadata parseMessageMetadata(String message) {
        try {
            // 分割消息获取数据库名、表名和数据
            String[] parts = message.split(":", 3);
            if (parts.length < 3) {
                logger.warn("消息格式不正确: {}", message);
                return null;
            }
            
            String databaseName = parts[0];
            String tableName = parts[1];
            String rawData = parts[2];
            
            // 从数据中提取操作类型
            String operationType = extractOperationType(rawData);
            
            return new MessageMetadata(databaseName, tableName, operationType, rawData);
        } catch (Exception e) {
            logger.error("解析消息元数据失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从消息中提取操作类型
     */
    private String extractOperationType(String rawData) {
        // 根据Binlog消息内容判断操作类型
        if (rawData.contains("WriteRowsEventData")) {
            return "insert";
        } else if (rawData.contains("UpdateRowsEventData")) {
            return "update";
        } else if (rawData.contains("DeleteRowsEventData")) {
            return "delete";
        }
        return "unknown";
    }

    /**
     * 解析事件数据
     */
    private JSONObject parseEventData(String rawData) {
        try {
            // 创建一个空的JSONObject用于存储解析后的数据
            JSONObject result = new JSONObject();
            
            // 以下是针对MySQL Binlog数据格式的简化解析逻辑
            // 实际应用中可能需要根据具体的数据格式进行调整
            
            // 尝试提取主键id
            if (rawData.contains("id=")) {
                int idStartPos = rawData.indexOf("id=") + 3;
                int idEndPos = rawData.indexOf(",", idStartPos);
                if (idEndPos > idStartPos) {
                    String idStr = rawData.substring(idStartPos, idEndPos).trim();
                    result.put("id", idStr);
                }
            }
            
            // 提取其他常见字段示例
            // 这里仅作为示例，实际应用中需要根据表结构提取相应字段
            extractCommonField(rawData, result, "name");
            extractCommonField(rawData, result, "price");
            extractCommonField(rawData, result, "status");
            extractCommonField(rawData, result, "create_time");
            extractCommonField(rawData, result, "update_time");
            
            return result;
        } catch (Exception e) {
            logger.error("解析事件数据失败: {}", e.getMessage());
            return new JSONObject();
        }
    }

    /**
     * 从原始数据中提取常见字段
     */
    private void extractCommonField(String rawData, JSONObject result, String fieldName) {
        try {
            String fieldKey = fieldName + "=";
            if (rawData.contains(fieldKey)) {
                int startPos = rawData.indexOf(fieldKey) + fieldKey.length();
                int endPos = rawData.indexOf(",", startPos);
                if (endPos > startPos) {
                    String fieldValue = rawData.substring(startPos, endPos).trim();
                    // 尝试转换数值类型
                    if (fieldValue.matches("\\d+")) {
                        result.put(fieldName, Integer.parseInt(fieldValue));
                    } else if (fieldValue.matches("\\d+\\.\\d+")) {
                        result.put(fieldName, Double.parseDouble(fieldValue));
                    } else {
                        result.put(fieldName, fieldValue);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("提取字段{}失败: {}", fieldName, e.getMessage());
        }
    }

    /**
     * 提取主键
     */
    private String extractPrimaryKey(JSONObject data) {
        try {
            // 尝试从数据中获取主键
            if (data.containsKey("id")) {
                return data.getString("id");
            }
            // 可以根据实际情况添加其他可能的主键字段
            if (data.containsKey("goods_id")) {
                return data.getString("goods_id");
            }
            if (data.containsKey("user_id")) {
                return data.getString("user_id");
            }
        } catch (JSONException e) {
            logger.error("提取主键失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取Redis键
     */
    private String getRedisKey(String tableName, String id) {
        // 使用表名到Redis键的映射，如果没有映射则使用默认格式
        String keyPrefix = tableKeyMap.getOrDefault(tableName, tableName + ":");
        return REDIS_KEY_PREFIX + keyPrefix + id;
    }

    /**
     * 消息元数据内部类
     */
    private static class MessageMetadata {
        private final String databaseName;
        private final String tableName;
        private final String operationType;
        private final String data;

        public MessageMetadata(String databaseName, String tableName, String operationType, String data) {
            this.databaseName = databaseName;
            this.tableName = tableName;
            this.operationType = operationType;
            this.data = data;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public String getTableName() {
            return tableName;
        }

        public String getOperationType() {
            return operationType;
        }

        public String getData() {
            return data;
        }
    }
}
