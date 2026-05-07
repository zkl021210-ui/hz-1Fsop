package com.sop.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sop.process.domain.ProcessEvent;
import com.sop.process.mapper.ProcessEventMapper;
import com.sop.process.service.ProcessEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 流程事件服务实现（仅读操作）
 *
 * @author SOP Team
 */
@Slf4j
@Service
public class ProcessEventServiceImpl implements ProcessEventService {

    private final ProcessEventMapper processEventMapper;

    public ProcessEventServiceImpl(ProcessEventMapper processEventMapper) {
        this.processEventMapper = processEventMapper;
    }

    @Override
    public ProcessEvent getByEventId(String eventId) {
        if (eventId == null || eventId.isEmpty()) return null;
        return processEventMapper.selectOne(
                new LambdaQueryWrapper<ProcessEvent>()
                        .eq(ProcessEvent::getEventId, eventId));
    }

    @Override
    public List<ProcessEvent> listFailedVisionErrors(String deviceSn) {
        return processEventMapper.selectList(
                new LambdaQueryWrapper<ProcessEvent>()
                        .eq(ProcessEvent::getDeviceSn, deviceSn)
                        .eq(ProcessEvent::getEventType, "VISION_SERVICE_ERROR")
                        .eq(ProcessEvent::getStatus, "FAILED")
                        .orderByDesc(ProcessEvent::getCreateTime)
                        .last("limit 10"));
    }

    @Override
    public List<ProcessEvent> listAllFailedVisionErrors() {
        return processEventMapper.selectList(
                new LambdaQueryWrapper<ProcessEvent>()
                        .eq(ProcessEvent::getEventType, "VISION_SERVICE_ERROR")
                        .eq(ProcessEvent::getStatus, "FAILED")
                        .orderByDesc(ProcessEvent::getCreateTime)
                        .last("limit 20"));
    }
}
