package com.sop.mq.publisher;

import com.sop.mq.message.SopProcessEventMessage;
import com.sop.process.domain.ProcessEvent;
import com.sop.process.mapper.ProcessEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * RabbitMQ 事件发布器实现
 * <p>
 * 支持 sop.mq.enabled 开关。发送失败不抛异常，在 process_event 中记录 MQ_PUBLISH_FAILED。
 *
 * @author SOP Team
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sop.mq.enabled", havingValue = "true")
public class RabbitSopEventPublisher implements SopEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ProcessEventMapper processEventMapper;

    @Value("${sop.mq.enabled:true}")
    private boolean mqEnabled;

    @Value("${sop.mq.exchange}")
    private String exchange;

    public RabbitSopEventPublisher(RabbitTemplate rabbitTemplate,
                                    ProcessEventMapper processEventMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.processEventMapper = processEventMapper;
    }

    @Override
    public void publish(SopProcessEventMessage message) {
        if (!mqEnabled) {
            log.debug("MQ 未启用，跳过消息发布: eventType={}, deviceSn={}", message.getEventType(), message.getDeviceSn());
            return;
        }

        if (message.getMessageId() == null || message.getMessageId().isEmpty()) {
            message.setMessageId(generateMessageId(message));
        }
        if (message.getOccurredTime() == null) {
            message.setOccurredTime(LocalDateTime.now());
        }
        if (message.getRetryCount() == null) {
            message.setRetryCount(0);
        }

        try {
            log.debug("发布MQ消息: messageId={}, eventType={}, routingKey={}",
                    message.getMessageId(), message.getEventType(), message.getRoutingKey());
            rabbitTemplate.convertAndSend(exchange, message.getRoutingKey(), message);
        } catch (Exception e) {
            log.error("MQ消息发布失败: messageId={}, eventType={}", message.getMessageId(), message.getEventType(), e);
            recordPublishFailed(message, e);
        }
    }

    private String generateMessageId(SopProcessEventMessage message) {
        if (message.getEventId() != null && !message.getEventId().isEmpty()) {
            return "MSG:" + message.getEventId();
        }
        return "MSG:" + message.getEventType() + ":"
                + (message.getDeviceSn() != null ? message.getDeviceSn() : "unknown") + ":"
                + (message.getTaskId() != null ? message.getTaskId() : 0) + ":"
                + (message.getStepRunId() != null ? message.getStepRunId() : 0) + ":"
                + System.currentTimeMillis();
    }

    private void recordPublishFailed(SopProcessEventMessage message, Exception e) {
        try {
            String failEventId = "MQ_PUBLISH_FAILED:" + message.getMessageId() + ":" + System.currentTimeMillis();
            ProcessEvent evt = new ProcessEvent();
            evt.setEventId(failEventId);
            evt.setTaskId(message.getTaskId());
            evt.setDeviceSn(message.getDeviceSn());
            evt.setStepId(message.getStepId());
            evt.setStepRunId(message.getStepRunId());
            evt.setEventType("MQ_PUBLISH_FAILED");
            evt.setStatus("FAILED");
            evt.setErrorMessage(e.getMessage());
            evt.setDuplicateCount(0);
            processEventMapper.insert(evt);
            log.info("记录MQ_PUBLISH_FAILED: messageId={}", message.getMessageId());
        } catch (Exception ex) {
            log.error("记录MQ_PUBLISH_FAILED失败: ", ex);
        }
    }
}
