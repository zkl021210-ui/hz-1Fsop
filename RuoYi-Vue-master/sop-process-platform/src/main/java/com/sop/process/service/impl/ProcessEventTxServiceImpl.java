package com.sop.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sop.mq.message.SopProcessEventMessage;
import com.sop.mq.publisher.SopEventPublisher;
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
 * 事件落库后发布 MQ 消息（仅日志，不影响主业务）。
 *
 * @author SOP Team
 */
@Slf4j
@Service
public class ProcessEventTxServiceImpl implements ProcessEventTxService {

    private final ProcessEventMapper processEventMapper;
    private final SopEventPublisher sopEventPublisher;

    public ProcessEventTxServiceImpl(ProcessEventMapper processEventMapper,
                                      @org.springframework.beans.factory.annotation.Autowired(required = false) SopEventPublisher sopEventPublisher) {
        this.processEventMapper = processEventMapper;
        this.sopEventPublisher = sopEventPublisher;
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
        publishEvent(evt);
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
        publishEvent(evt);
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
        publishEvent(evt);
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

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordVisionError(String deviceSn, String errorMsg, String errorType, String payload) {
        String eventId = "VISION_ERROR:" + deviceSn + ":" + System.currentTimeMillis();
        ProcessEvent evt = new ProcessEvent();
        evt.setEventId(eventId);
        evt.setDeviceSn(deviceSn);
        evt.setEventType("VISION_SERVICE_ERROR");
        evt.setStatus("FAILED");
        evt.setErrorMessage(errorMsg);
        evt.setResult(errorType);
        evt.setPayload(payload);
        evt.setDuplicateCount(0);
        processEventMapper.insert(evt);
        log.info("记录VISION_SERVICE_ERROR(含载荷): eventId={}, errorType={}, error={}", eventId, errorType, errorMsg);
        publishEvent(evt);
        publishVisionRetry(evt);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordStepTimeout(String eventId, Long taskId, String deviceSn, Long stepId,
                                  Long stepRunId, String payload) {
        ProcessEvent evt = new ProcessEvent();
        evt.setEventId(eventId);
        evt.setTaskId(taskId);
        evt.setDeviceSn(deviceSn);
        evt.setStepId(stepId);
        evt.setStepRunId(stepRunId);
        evt.setEventType("STEP_TIMEOUT");
        evt.setStatus("FAILED");
        evt.setPayload(payload);
        evt.setDuplicateCount(0);
        processEventMapper.insert(evt);
        log.info("记录STEP_TIMEOUT: eventId={}, taskId={}, stepId={}", eventId, taskId, stepId);
        publishEvent(evt);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordTaskStarted(Long taskId, String deviceSn, String modelCode) {
        String eventId = "TASK_STARTED:" + taskId + ":" + deviceSn;
        ProcessEvent evt = new ProcessEvent();
        evt.setEventId(eventId);
        evt.setTaskId(taskId);
        evt.setDeviceSn(deviceSn);
        evt.setEventType("TASK_STARTED");
        evt.setStatus("SUCCESS");
        evt.setResult(modelCode);
        evt.setDuplicateCount(0);
        processEventMapper.insert(evt);
        log.info("记录TASK_STARTED: taskId={}, deviceSn={}", taskId, deviceSn);
        publishEvent(evt);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordStepStarted(Long taskId, String deviceSn, Long stepId, Long stepRunId, Integer stepNo) {
        String eventId = "STEP_STARTED:" + stepRunId + ":" + taskId;
        ProcessEvent evt = new ProcessEvent();
        evt.setEventId(eventId);
        evt.setTaskId(taskId);
        evt.setDeviceSn(deviceSn);
        evt.setStepId(stepId);
        evt.setStepRunId(stepRunId);
        evt.setEventType("STEP_STARTED");
        evt.setStatus("SUCCESS");
        evt.setResult("stepNo=" + stepNo);
        evt.setDuplicateCount(0);
        processEventMapper.insert(evt);
        log.info("记录STEP_STARTED: taskId={}, stepRunId={}", taskId, stepRunId);
        publishEvent(evt);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordQualityTraceCreated(Long taskId, String deviceSn, String finalResult) {
        String eventId = "QUALITY_TRACE:" + taskId + ":" + System.currentTimeMillis();
        ProcessEvent evt = new ProcessEvent();
        evt.setEventId(eventId);
        evt.setTaskId(taskId);
        evt.setDeviceSn(deviceSn);
        evt.setEventType("QUALITY_TRACE_CREATED");
        evt.setStatus("SUCCESS");
        evt.setResult(finalResult);
        evt.setDuplicateCount(0);
        processEventMapper.insert(evt);
        log.info("记录QUALITY_TRACE_CREATED: taskId={}, result={}", taskId, finalResult);
        publishEvent(evt);
    }

    // ==================== MQ 发布 ====================

    private void publishEvent(ProcessEvent evt) {
        if (sopEventPublisher == null) return;
        try {
            SopProcessEventMessage msg = SopProcessEventMessage.builder()
                    .eventId(evt.getEventId())
                    .eventType(evt.getEventType())
                    .routingKey(toRoutingKey(evt.getEventType()))
                    .taskId(evt.getTaskId())
                    .deviceSn(evt.getDeviceSn())
                    .stepId(evt.getStepId())
                    .stepRunId(evt.getStepRunId())
                    .payload(evt.getPayload())
                    .occurredTime(LocalDateTime.now())
                    .retryCount(0)
                    .build();
            sopEventPublisher.publish(msg);
        } catch (Exception e) {
            log.error("MQ发布异常(event落库已成功): eventId={}, eventType={}", evt.getEventId(), evt.getEventType(), e);
        }
    }

    private void publishVisionRetry(ProcessEvent evt) {
        if (sopEventPublisher == null) return;
        try {
            SopProcessEventMessage msg = SopProcessEventMessage.builder()
                    .eventId(evt.getEventId())
                    .eventType(evt.getEventType())
                    .routingKey("sop.vision.retry")
                    .taskId(evt.getTaskId())
                    .deviceSn(evt.getDeviceSn())
                    .stepId(evt.getStepId())
                    .stepRunId(evt.getStepRunId())
                    .payload(evt.getPayload())
                    .occurredTime(LocalDateTime.now())
                    .retryCount(0)
                    .build();
            sopEventPublisher.publish(msg);
        } catch (Exception e) {
            log.error("视觉重试MQ发布异常: eventId={}", evt.getEventId(), e);
        }
    }

    private String toRoutingKey(String eventType) {
        if (eventType == null) return "sop.event.unknown";
        switch (eventType) {
            case "TASK_STARTED":         return "sop.event.task.started";
            case "STEP_STARTED":         return "sop.event.step.started";
            case "AI_PASS_RECEIVED":     return "sop.event.ai.pass";
            case "STEP_FINISHED":        return "sop.event.step.finished";
            case "TASK_COMPLETED":       return "sop.event.task.completed";
            case "QUALITY_TRACE_CREATED": return "sop.event.task.completed";
            case "VISION_SERVICE_ERROR": return "sop.event.vision.error";
            case "STEP_TIMEOUT":         return "sop.event.step.timeout";
            case "CALLBACK_REJECTED":    return "sop.event.ai.pass";
            default:                     return "sop.event." + eventType.toLowerCase();
        }
    }
}
