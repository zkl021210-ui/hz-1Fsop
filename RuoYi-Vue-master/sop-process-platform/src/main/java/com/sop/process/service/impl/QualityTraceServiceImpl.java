package com.sop.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sop.process.domain.QualityTrace;
import com.sop.process.mapper.QualityTraceMapper;
import com.sop.process.service.QualityTraceService;
import org.springframework.stereotype.Service;

/**
 * 质量追溯 Service 实现
 *
 * @author SOP Team
 */
@Service
public class QualityTraceServiceImpl extends ServiceImpl<QualityTraceMapper, QualityTrace> implements QualityTraceService {

    @Override
    public QualityTrace getByTaskId(Long taskId) {
        LambdaQueryWrapper<QualityTrace> wrapper = new LambdaQueryWrapper<QualityTrace>()
                .eq(QualityTrace::getTaskId, taskId)
                .eq(QualityTrace::getDeleted, 0);
        return this.getOne(wrapper);
    }

    @Override
    public QualityTrace createTrace(QualityTrace trace) {
        this.save(trace);
        return trace;
    }
}
