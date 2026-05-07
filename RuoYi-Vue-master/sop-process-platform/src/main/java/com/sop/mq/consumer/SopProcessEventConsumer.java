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
 * SOP 过程事件消费者
 * <p>
 * 消费 sop.process.event.queue 中的过程事件，当前阶段做轻量日志记录和预留统计入口。
 * 不修改主业务状态。
 *
 * @author SOP Team
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sop.mq.enabled", havingValue = "true")
public class SopProcessEventConsumer {

    private static final String CONSUMER_NAME = "SopProcessEventConsumer";
    private static final int MAX_RETRY = 3;

    private final MqMessageLogService mqMessageLogService;

    public SopProcessEventConsumer(MqMessageLogService mqMessageLogService) {
        this.mqMessageLogService = mqMessageLogService;
    }

    @RabbitListener(queues = "${sop.mq.event-queue}")
    public void onMessage(SopProcessEventMessage message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        String messageId = message.getMessageId();

        try {
            MqMessageLog existing = mqMessageLogService.getByMessageIdAndConsumer(messageId, CONSUMER_NAME);
            if (existing != null && "SUCCESS".equals(existing.getStatus())) {
                log.debug("消息已成功消费，直接ack: messageId={}", messageId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (existing == null) {
                mqMessageLogService.recordProcessing(messageId, message.getEventId(),
                        message.getEventType(), CONSUMER_NAME);
            }

            switch (message.getEventType()) {
                case "TASK_STARTED":
                    log.info("MQ消费-任务开工: taskId={}, deviceSn={}", message.getTaskId(), message.getDeviceSn());
                    break;
                case "STEP_STARTED":
                    log.info("MQ消费-步骤启动: stepId={}, stepRunId={}", message.getStepId(), message.getStepRunId());
                    break;
                case "AI_PASS_RECEIVED":
                    log.info("MQ消费-AI过站回调: eventId={}, stepRunId={}", message.getEventId(), message.getStepRunId());
                    break;
                case "STEP_FINISHED":
                    log.info("MQ消费-步骤完成: stepId={}, stepRunId={}", message.getStepId(), message.getStepRunId());
                    break;
                case "TASK_COMPLETED":
                    log.info("MQ消费-任务完成: taskId={}, deviceSn={}", message.getTaskId(), message.getDeviceSn());
                    break;
                case "STEP_TIMEOUT":
                    log.warn("MQ消费-步骤超时: taskId={}, stepRunId={}", message.getTaskId(), message.getStepRunId());
                    break;
                case "VISION_SERVICE_ERROR":
                    log.error("MQ消费-视觉服务错误: deviceSn={}, payload={}", message.getDeviceSn(), message.getPayload());
                    break;
                case "CALLBACK_REJECTED":
                    log.warn("MQ消费-回调被拒: eventId={}", message.getEventId());
                    break;
                default:
                    log.info("MQ消费-未知事件: type={}, messageId={}", message.getEventType(), messageId);
            }

            mqMessageLogService.markSuccess(messageId, CONSUMER_NAME);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("MQ消费异常: messageId={}, eventType={}", messageId, message.getEventType(), e);
            mqMessageLogService.markFailed(messageId, CONSUMER_NAME, e.getMessage());

            MqMessageLog failed = mqMessageLogService.getByMessageIdAndConsumer(messageId, CONSUMER_NAME);
            int retryCount = failed != null && failed.getRetryCount() != null ? failed.getRetryCount() : 0;

            if (retryCount >= MAX_RETRY) {
                log.error("消息已达最大重试次数，拒绝不重入: messageId={}", messageId);
                channel.basicReject(deliveryTag, false);
            } else {
                log.warn("消息消费失败，重入队列: messageId={}, retryCount={}", messageId, retryCount);
                channel.basicNack(deliveryTag, false, true);
            }
        }
    }
}
