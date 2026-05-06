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
}
