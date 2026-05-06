package com.sop.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sop.process.domain.ProcessEvent;
import com.sop.process.mapper.ProcessEventMapper;
import com.sop.process.service.ProcessEventTxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 流程事件独立事务服务实现
 * <p>
 * 所有写操作使用 REQUIRES_NEW，确保事件落库不受主事务回滚影响。
 *
 * @author SOP Team
 */
@Slf4j
@Service
public class ProcessEventTxServiceImpl implements ProcessEventTxService {

    private final ProcessEventMapper processEventMapper;

    public ProcessEventTxServiceImpl(ProcessEventMapper processEventMapper) {
        this.processEventMapper = processEventMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProcessEvent tryCreateProcessing(String eventId, Long taskId, String deviceSn,
                                            Long stepId, Long stepRunId, String eventType, String payload) {
        ProcessEvent evt = new ProcessEvent();
        evt.setEventId(eventId);
        evt.setTaskId(taskId);
        evt.setDeviceSn(deviceSn);
        evt.setStepId(stepId);
        evt.setStepRunId(stepRunId);
        evt.setEventType(eventType);
        evt.setStatus("PROCESSING");
        evt.setPayload(payload);
        evt.setDuplicateCount(0);
        processEventMapper.insert(evt);
        log.info("创建PROCESSING事件: eventId={}, taskId={}", eventId, taskId);
        return evt;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDuplicate(String eventId) {
        ProcessEvent existing = processEventMapper.selectOne(
                new LambdaQueryWrapper<ProcessEvent>()
                        .eq(ProcessEvent::getEventId, eventId));
        if (existing == null) return;
        int count = existing.getDuplicateCount() != null ? existing.getDuplicateCount() + 1 : 1;
        existing.setDuplicateCount(count);
        existing.setLastReceivedTime(LocalDateTime.now());
        processEventMapper.updateById(existing);
        log.info("重复回调更新: eventId={}, duplicateCount={}", eventId, count);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(String eventId, String result) {
        ProcessEvent existing = processEventMapper.selectOne(
                new LambdaQueryWrapper<ProcessEvent>()
                        .eq(ProcessEvent::getEventId, eventId));
        if (existing == null) return;
        existing.setStatus("SUCCESS");
        existing.setProcessedTime(LocalDateTime.now());
        if (result != null) {
            existing.setResult(result);
        }
        processEventMapper.updateById(existing);
        log.info("事件标记SUCCESS: eventId={}", eventId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String eventId, String errorMessage) {
        ProcessEvent existing = processEventMapper.selectOne(
                new LambdaQueryWrapper<ProcessEvent>()
                        .eq(ProcessEvent::getEventId, eventId));
        if (existing == null) return;
        existing.setStatus("FAILED");
        existing.setErrorMessage(errorMessage);
        existing.setProcessedTime(LocalDateTime.now());
        processEventMapper.updateById(existing);
        log.info("事件标记FAILED: eventId={}, error={}", eventId, errorMessage);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(String eventId) {
        ProcessEvent existing = processEventMapper.selectOne(
                new LambdaQueryWrapper<ProcessEvent>()
                        .eq(ProcessEvent::getEventId, eventId));
        if (existing == null) return;
        existing.setStatus("PROCESSING");
        existing.setErrorMessage(null);
        existing.setProcessedTime(null);
        processEventMapper.updateById(existing);
        log.info("事件重置PROCESSING(重试): eventId={}", eventId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRejected(String eventId, String deviceSn, Long taskId,
                               Long stepId, Long stepRunId, String rejectReason) {
        ProcessEvent evt = new ProcessEvent();
        evt.setEventId(eventId);
        evt.setTaskId(taskId);
        evt.setDeviceSn(deviceSn);
        evt.setStepId(stepId);
        evt.setStepRunId(stepRunId);
        evt.setEventType("CALLBACK_REJECTED");
        evt.setStatus("IGNORED");
        evt.setResult(rejectReason);
        evt.setDuplicateCount(0);
        processEventMapper.insert(evt);
        log.info("记录CALLBACK_REJECTED: eventId={}, reason={}", eventId, rejectReason);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEvent(String eventId, Long taskId, String deviceSn, Long stepId,
                            Long stepRunId, String eventType, String status, String result) {
        ProcessEvent evt = new ProcessEvent();
        evt.setEventId(eventId);
        evt.setTaskId(taskId);
        evt.setDeviceSn(deviceSn);
        evt.setStepId(stepId);
        evt.setStepRunId(stepRunId);
        evt.setEventType(eventType);
        evt.setStatus(status);
        evt.setResult(result);
        evt.setDuplicateCount(0);
        processEventMapper.insert(evt);
        log.info("记录事件: eventId={}, type={}, status={}", eventId, eventType, status);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordError(String deviceSn, String errorMsg) {
        String eventId = "VISION_ERROR:" + deviceSn + ":" + System.currentTimeMillis();
        ProcessEvent evt = new ProcessEvent();
        evt.setEventId(eventId);
        evt.setDeviceSn(deviceSn);
        evt.setEventType("VISION_SERVICE_ERROR");
        evt.setStatus("FAILED");
        evt.setErrorMessage(errorMsg);
        evt.setDuplicateCount(0);
        processEventMapper.insert(evt);
        log.info("记录VISION_SERVICE_ERROR: eventId={}, error={}", eventId, errorMsg);
    }
}
