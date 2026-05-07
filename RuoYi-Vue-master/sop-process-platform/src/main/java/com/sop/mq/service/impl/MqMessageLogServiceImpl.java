package com.sop.mq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sop.mq.domain.MqMessageLog;
import com.sop.mq.mapper.MqMessageLogMapper;
import com.sop.mq.service.MqMessageLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * MQ 消息日志服务实现（REQUIRES_NEW 事务隔离）
 *
 * @author SOP Team
 */
@Slf4j
@Service
public class MqMessageLogServiceImpl implements MqMessageLogService {

    private final MqMessageLogMapper mqMessageLogMapper;

    public MqMessageLogServiceImpl(MqMessageLogMapper mqMessageLogMapper) {
        this.mqMessageLogMapper = mqMessageLogMapper;
    }

    @Override
    public MqMessageLog getByMessageIdAndConsumer(String messageId, String consumerName) {
        return mqMessageLogMapper.selectOne(
                new LambdaQueryWrapper<MqMessageLog>()
                        .eq(MqMessageLog::getMessageId, messageId)
                        .eq(MqMessageLog::getConsumerName, consumerName));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordProcessing(String messageId, String eventId, String eventType, String consumerName) {
        MqMessageLog logEntry = new MqMessageLog();
        logEntry.setMessageId(messageId);
        logEntry.setEventId(eventId);
        logEntry.setEventType(eventType);
        logEntry.setConsumerName(consumerName);
        logEntry.setStatus("PROCESSING");
        logEntry.setRetryCount(0);
        mqMessageLogMapper.insert(logEntry);
        log.debug("记录MQ消费开始: messageId={}, consumer={}", messageId, consumerName);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(String messageId, String consumerName) {
        MqMessageLog existing = getByMessageIdAndConsumer(messageId, consumerName);
        if (existing != null) {
            existing.setStatus("SUCCESS");
            existing.setUpdateTime(LocalDateTime.now());
            mqMessageLogMapper.updateById(existing);
            log.debug("标记MQ消费成功: messageId={}, consumer={}", messageId, consumerName);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String messageId, String consumerName, String errorMessage) {
        MqMessageLog existing = getByMessageIdAndConsumer(messageId, consumerName);
        if (existing != null) {
            existing.setStatus("FAILED");
            existing.setErrorMessage(errorMessage);
            existing.setRetryCount(existing.getRetryCount() != null ? existing.getRetryCount() + 1 : 1);
            existing.setUpdateTime(LocalDateTime.now());
            mqMessageLogMapper.updateById(existing);
        } else {
            MqMessageLog logEntry = new MqMessageLog();
            logEntry.setMessageId(messageId);
            logEntry.setConsumerName(consumerName);
            logEntry.setStatus("FAILED");
            logEntry.setErrorMessage(errorMessage);
            logEntry.setRetryCount(1);
            mqMessageLogMapper.insert(logEntry);
        }
        log.debug("标记MQ消费失败: messageId={}, consumer={}", messageId, consumerName);
    }
}
