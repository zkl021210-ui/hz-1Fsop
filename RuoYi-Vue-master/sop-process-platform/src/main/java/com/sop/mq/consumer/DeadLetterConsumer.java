package com.sop.mq.consumer;

import com.rabbitmq.client.Channel;
import com.sop.mq.domain.MqMessageLog;
import com.sop.mq.message.SopProcessEventMessage;
import com.sop.mq.service.MqMessageLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 死信队列消费者
 * <p>
 * 记录最终消费失败的消息，打日志。
 *
 * @author SOP Team
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sop.mq.enabled", havingValue = "true")
public class DeadLetterConsumer {

    private static final String CONSUMER_NAME = "DeadLetterConsumer";

    private final MqMessageLogService mqMessageLogService;

    public DeadLetterConsumer(MqMessageLogService mqMessageLogService) {
        this.mqMessageLogService = mqMessageLogService;
    }

    @RabbitListener(queues = "${sop.mq.dead-letter-queue}")
    public void onDeadLetter(SopProcessEventMessage message, Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        String messageId = message.getMessageId();

        try {
            log.error("死信消息: messageId={}, eventType={}, deviceSn={}, taskId={}",
                    messageId, message.getEventType(), message.getDeviceSn(), message.getTaskId());

            mqMessageLogService.markFailed(messageId, CONSUMER_NAME,
                    "消息进入死信队列: eventType=" + message.getEventType());

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("死信处理异常: messageId={}", messageId, e);
            channel.basicAck(deliveryTag, false);
        }
    }
}
