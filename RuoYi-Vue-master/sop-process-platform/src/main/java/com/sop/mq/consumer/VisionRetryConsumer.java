package com.sop.mq.consumer;

import com.rabbitmq.client.Channel;
import com.sop.integration.vision.VisionGateway;
import com.sop.integration.vision.VisionResult;
import com.sop.mq.domain.MqMessageLog;
import com.sop.mq.message.SopProcessEventMessage;
import com.sop.mq.service.MqMessageLogService;
import com.sop.process.domain.ProcessEvent;
import com.sop.process.service.ProcessEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 视觉服务重试消费者
 * <p>
 * 消费 sop.vision.retry.queue，调用 VisionGateway.retryLastFailed 进行异步重试。
 *
 * @author SOP Team
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sop.mq.enabled", havingValue = "true")
public class VisionRetryConsumer {

    private static final String CONSUMER_NAME = "VisionRetryConsumer";
    private static final int MAX_RETRY = 3;

    private final MqMessageLogService mqMessageLogService;
    private final VisionGateway visionGateway;
    private final ProcessEventService processEventService;

    public VisionRetryConsumer(MqMessageLogService mqMessageLogService,
                               VisionGateway visionGateway,
                               ProcessEventService processEventService) {
        this.mqMessageLogService = mqMessageLogService;
        this.visionGateway = visionGateway;
        this.processEventService = processEventService;
    }

    @RabbitListener(queues = "${sop.mq.vision-retry-queue}")
    public void onMessage(SopProcessEventMessage message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        String messageId = message.getMessageId();

        try {
            MqMessageLog existing = mqMessageLogService.getByMessageIdAndConsumer(messageId, CONSUMER_NAME);
            if (existing != null && "SUCCESS".equals(existing.getStatus())) {
                log.debug("视觉重试消息已成功消费，直接ack: messageId={}", messageId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (existing == null) {
                mqMessageLogService.recordProcessing(messageId, message.getEventId(),
                        message.getEventType(), CONSUMER_NAME);
            }

            String deviceSn = message.getDeviceSn();
            if (deviceSn == null || deviceSn.isEmpty()) {
                log.warn("视觉重试消息缺少deviceSn: messageId={}", messageId);
                mqMessageLogService.markFailed(messageId, CONSUMER_NAME, "缺少 deviceSn");
                channel.basicAck(deliveryTag, false);
                return;
            }

            log.info("开始异步重试视觉服务: deviceSn={}, eventId={}", deviceSn, message.getEventId());
            VisionResult result = visionGateway.retryLastFailed(deviceSn);

            ProcessEvent event = processEventService.getByEventId(message.getEventId());
            if (result.isSuccess()) {
                log.info("视觉重试成功: deviceSn={}, eventId={}", deviceSn, message.getEventId());
                mqMessageLogService.markSuccess(messageId, CONSUMER_NAME);
            } else {
                log.warn("视觉重试失败: deviceSn={}, eventId={}, reason={}",
                        deviceSn, message.getEventId(), result.getErrorMessage());
                mqMessageLogService.markFailed(messageId, CONSUMER_NAME,
                        result.getErrorMessage() != null ? result.getErrorMessage() : "重试失败");
            }

            int retryCount = message.getRetryCount() != null ? message.getRetryCount() + 1 : 1;
            if (retryCount >= MAX_RETRY) {
                log.error("视觉重试已达最大次数，进入死信: deviceSn={}, eventId={}", deviceSn, message.getEventId());
                channel.basicReject(deliveryTag, false);
            } else {
                channel.basicAck(deliveryTag, false);
            }
        } catch (Exception e) {
            log.error("视觉重试消费异常: messageId={}", messageId, e);
            mqMessageLogService.markFailed(messageId, CONSUMER_NAME, e.getMessage());

            MqMessageLog failed = mqMessageLogService.getByMessageIdAndConsumer(messageId, CONSUMER_NAME);
            int retryCount = failed != null && failed.getRetryCount() != null ? failed.getRetryCount() : 0;

            if (retryCount >= MAX_RETRY) {
                channel.basicReject(deliveryTag, false);
            } else {
                channel.basicNack(deliveryTag, false, true);
            }
        }
    }
}
