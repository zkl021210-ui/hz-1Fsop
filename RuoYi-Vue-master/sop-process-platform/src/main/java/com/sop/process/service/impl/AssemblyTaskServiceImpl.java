package com.sop.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sop.common.exception.BusinessException;
import com.sop.common.exception.ErrorCode;
import com.sop.process.domain.AssemblyTask;
import com.sop.process.dto.AssemblyTaskDTO;
import com.sop.process.mapper.AssemblyTaskMapper;
import com.sop.process.service.AssemblyTaskService;
import com.sop.process.vo.AssemblyTaskVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class AssemblyTaskServiceImpl extends ServiceImpl<AssemblyTaskMapper, AssemblyTask> implements AssemblyTaskService {

    @Override
    public Page<AssemblyTaskVO> pageAssemblyTasks(int page, int pageSize, String modelCode, String status) {
        Page<AssemblyTask> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AssemblyTask> wrapper = new LambdaQueryWrapper<AssemblyTask>()
                .eq(AssemblyTask::getDeleted, 0)
                .eq(modelCode != null && !modelCode.isEmpty(), AssemblyTask::getModelCode, modelCode)
                .eq(status != null && !status.isEmpty(), AssemblyTask::getStatus, status)
                .orderByDesc(AssemblyTask::getCreateTime);
        Page<AssemblyTask> result = this.page(pageParam, wrapper);
        Page<AssemblyTaskVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public AssemblyTaskVO getAssemblyTaskById(Long taskId) {
        AssemblyTask entity = this.getById(taskId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "装配任务不存在");
        }
        return toVO(entity);
    }

    @Override
    public AssemblyTaskVO createAssemblyTask(AssemblyTaskDTO dto) {
        AssemblyTask entity = new AssemblyTask();
        BeanUtils.copyProperties(dto, entity);
        this.save(entity);
        return toVO(entity);
    }

    @Override
    public AssemblyTaskVO updateAssemblyTask(Long taskId, AssemblyTaskDTO dto) {
        AssemblyTask entity = this.getById(taskId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "装配任务不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setTaskId(taskId);
        this.updateById(entity);
        return toVO(entity);
    }

    @Override
    public void deleteAssemblyTask(Long taskId) {
        AssemblyTask entity = this.getById(taskId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "装配任务不存在");
        }
        entity.setDeleted(1);
        this.updateById(entity);
    }

    private AssemblyTaskVO toVO(AssemblyTask entity) {
        AssemblyTaskVO vo = new AssemblyTaskVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
