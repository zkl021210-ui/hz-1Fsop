package com.sop.process.service;

import com.sop.process.domain.ProcessEvent;

/**
 * 流程事件独立事务服务（REQUIRES_NEW）
 * <p>
 * 所有流程事件写入通过此服务，确保独立事务落库，不受主事务回滚影响。
 *
 * @author SOP Team
 */
public interface ProcessEventTxService {

    ProcessEvent tryCreateProcessing(String eventId, Long taskId, String deviceSn,
                                     Long stepId, Long stepRunId, String eventType, String payload);

    void updateDuplicate(String eventId);

    void markSuccess(String eventId, String result);

    void markFailed(String eventId, String errorMessage);

    void markProcessing(String eventId);

    void recordRejected(String eventId, String deviceSn, Long taskId,
                        Long stepId, Long stepRunId, String rejectReason);

    void recordEvent(String eventId, Long taskId, String deviceSn, Long stepId,
                     Long stepRunId, String eventType, String status, String result);

    void recordError(String deviceSn, String errorMsg);

    /**
     * 记录视觉服务错误（含操作类型和请求载荷，用于后续重试）
     */
    void recordVisionError(String deviceSn, String errorMsg, String errorType, String payload);

    /**
     * 记录步骤超时事件
     */
    void recordStepTimeout(String eventId, Long taskId, String deviceSn, Long stepId,
                           Long stepRunId, String payload);

    /**
     * 记录任务开工事件
     */
    void recordTaskStarted(Long taskId, String deviceSn, String modelCode);

    /**
     * 记录步骤启动事件
     */
    void recordStepStarted(Long taskId, String deviceSn, Long stepId, Long stepRunId, Integer stepNo);

    /**
     * 记录质量追溯创建事件
     */
    void recordQualityTraceCreated(Long taskId, String deviceSn, String finalResult);
}
