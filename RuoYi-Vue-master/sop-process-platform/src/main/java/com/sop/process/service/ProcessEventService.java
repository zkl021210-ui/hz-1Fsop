package com.sop.process.service;

import com.sop.process.domain.ProcessEvent;

import java.util.List;

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

    /**
     * 查询指定设备的视觉服务失败事件（最近10条）
     */
    List<ProcessEvent> listFailedVisionErrors(String deviceSn);

    /**
     * 查询所有视觉服务失败事件（最近20条）
     */
    List<ProcessEvent> listAllFailedVisionErrors();
}
