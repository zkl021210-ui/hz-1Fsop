package com.sop.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 拓扑配置
 * <p>
 * sop.mq.enabled=false 时不创建任何 MQ Bean。
 * 如 RabbitMQ 不可用，需同时注释 application.yml 中 spring.rabbitmq 配置段。
 * <p>
 * 定义交换机、队列和绑定关系。使用 topic 交换机进行路由。
 *
 * @author SOP Team
 */
@Configuration
@ConditionalOnProperty(name = "sop.mq.enabled", havingValue = "true")
public class RabbitMqConfig {

    @Value("${sop.mq.exchange}")
    private String processExchange;

    @Value("${sop.mq.event-queue}")
    private String eventQueue;

    @Value("${sop.mq.vision-retry-queue}")
    private String visionRetryQueue;

    @Value("${sop.mq.dead-letter-exchange}")
    private String deadLetterExchange;

    @Value("${sop.mq.dead-letter-queue}")
    private String deadLetterQueue;

    // ==================== 交换机 ====================

    @Bean
    public TopicExchange processExchange() {
        return new TopicExchange(processExchange, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchange, true, false);
    }

    // ==================== 队列 ====================

    @Bean
    public Queue processEventQueue() {
        return QueueBuilder.durable(eventQueue)
                .deadLetterExchange(deadLetterExchange)
                .build();
    }

    @Bean
    public Queue visionRetryQueue() {
        return QueueBuilder.durable(visionRetryQueue)
                .deadLetterExchange(deadLetterExchange)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(deadLetterQueue).build();
    }

    // ==================== 绑定 ====================

    @Bean
    public Binding processEventBinding() {
        return BindingBuilder.bind(processEventQueue())
                .to(processExchange())
                .with("sop.event.#");
    }

    @Bean
    public Binding visionRetryBinding() {
        return BindingBuilder.bind(visionRetryQueue())
                .to(processExchange())
                .with("sop.vision.retry");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("#");
    }

    // ==================== 消息转换器 ====================

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
