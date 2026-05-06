package com.sop.process.service;

import com.sop.process.domain.QualityTrace;

/**
 * 质量追溯 Service 接口
 *
 * @author SOP Team
 */
public interface QualityTraceService {

    /**
     * 根据任务ID查询追溯记录
     */
    QualityTrace getByTaskId(Long taskId);

    /**
     * 创建质量追溯记录
     */
    QualityTrace createTrace(QualityTrace trace);
}
