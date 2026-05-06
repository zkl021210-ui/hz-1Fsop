package com.sop.process.service;

import com.sop.process.domain.ProcessEvent;

/**
 * 流程事件服务（读操作）
 * <p>
 * 所有写操作委托给 {@link ProcessEventTxService}，确保独立事务落库。
 *
 * @author SOP Team
 */
public interface ProcessEventService {

    /**
     * 按 eventId 查询
     */
    ProcessEvent getByEventId(String eventId);
}
