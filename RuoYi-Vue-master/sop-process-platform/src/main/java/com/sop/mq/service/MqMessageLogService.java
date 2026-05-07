package com.sop.mq.service;

import com.sop.mq.domain.MqMessageLog;

/**
 * MQ 消息日志服务
 * <p>
 * 消费幂等：根据 messageId + consumerName 判断消息是否已消费。
 *
 * @author SOP Team
 */
public interface MqMessageLogService {

    /**
     * 查询消费记录
     */
    MqMessageLog getByMessageIdAndConsumer(String messageId, String consumerName);

    /**
     * 记录消费开始（PROCESSING）
     */
    void recordProcessing(String messageId, String eventId, String eventType, String consumerName);

    /**
     * 标记消费成功
     */
    void markSuccess(String messageId, String consumerName);

    /**
     * 标记消费失败
     */
    void markFailed(String messageId, String consumerName, String errorMessage);
}
