package com.sop.mq.publisher;

import com.sop.mq.message.SopProcessEventMessage;

/**
 * SOP 事件消息发布器
 *
 * @author SOP Team
 */
public interface SopEventPublisher {

    /**
     * 发布事件消息到 RabbitMQ
     * <p>
     * 发送失败时记录日志，不影响主业务。
     *
     * @param message 事件消息
     */
    void publish(SopProcessEventMessage message);
}
