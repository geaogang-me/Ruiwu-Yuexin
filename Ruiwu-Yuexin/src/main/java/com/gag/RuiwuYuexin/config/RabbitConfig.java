package com.gag.RuiwuYuexin.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : 葛澳港
 * @description : RabbitMQ配置类，用于配置binlog相关的交换机、队列和绑定
 * @createDate : 2025-09-01 8:35
 */
@Configuration
public class RabbitConfig {

    // 交换机名称
    public static final String BINLOG_EXCHANGE = "binlog.exchange";
    // 队列名称
    public static final String BINLOG_QUEUE = "binlog.queue";
    // 路由键前缀
    public static final String ROUTING_KEY_PREFIX = "binlog.";

    @Bean
    public DirectExchange binlogExchange() {
        return new DirectExchange(BINLOG_EXCHANGE, true, false);
    }

    @Bean
    public Queue binlogQueue() {
        // 持久化队列
        return new Queue(BINLOG_QUEUE, true, false, false);
    }

    // 为insert、update、delete操作创建绑定
    @Bean
    public Binding insertBinding() {
        return BindingBuilder.bind(binlogQueue()).to(binlogExchange()).with(ROUTING_KEY_PREFIX + "insert");
    }

    @Bean
    public Binding updateBinding() {
        return BindingBuilder.bind(binlogQueue()).to(binlogExchange()).with(ROUTING_KEY_PREFIX + "update");
    }

    @Bean
    public Binding deleteBinding() {
        return BindingBuilder.bind(binlogQueue()).to(binlogExchange()).with(ROUTING_KEY_PREFIX + "delete");
    }
}
