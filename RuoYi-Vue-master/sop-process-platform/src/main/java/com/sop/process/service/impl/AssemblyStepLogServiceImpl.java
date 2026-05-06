package com.sop.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sop.common.exception.BusinessException;
import com.sop.common.exception.ErrorCode;
import com.sop.process.domain.AssemblyStepLog;
import com.sop.process.dto.AssemblyStepLogDTO;
import com.sop.process.mapper.AssemblyStepLogMapper;
import com.sop.process.service.AssemblyStepLogService;
import com.sop.process.vo.AssemblyStepLogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class AssemblyStepLogServiceImpl extends ServiceImpl<AssemblyStepLogMapper, AssemblyStepLog> implements AssemblyStepLogService {

    @Override
    public Page<AssemblyStepLogVO> pageAssemblyStepLogs(int page, int pageSize, Long taskId, String status) {
        Page<AssemblyStepLog> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AssemblyStepLog> wrapper = new LambdaQueryWrapper<AssemblyStepLog>()
                .eq(AssemblyStepLog::getDeleted, 0)
                .eq(taskId != null, AssemblyStepLog::getTaskId, taskId)
                .eq(status != null && !status.isEmpty(), AssemblyStepLog::getStatus, status)
                .orderByDesc(AssemblyStepLog::getCreateTime);
        Page<AssemblyStepLog> result = this.page(pageParam, wrapper);
        Page<AssemblyStepLogVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public AssemblyStepLogVO getAssemblyStepLogById(Long id) {
        AssemblyStepLog entity = this.getById(id);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "装配步骤日志不存在");
        }
        return toVO(entity);
    }

    @Override
    public AssemblyStepLogVO createAssemblyStepLog(AssemblyStepLogDTO dto) {
        AssemblyStepLog entity = new AssemblyStepLog();
        BeanUtils.copyProperties(dto, entity);
        this.save(entity);
        return toVO(entity);
    }

    @Override
    public AssemblyStepLogVO updateAssemblyStepLog(Long id, AssemblyStepLogDTO dto) {
        AssemblyStepLog entity = this.getById(id);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "装配步骤日志不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        this.updateById(entity);
        return toVO(entity);
    }

    @Override
    public void deleteAssemblyStepLog(Long id) {
        AssemblyStepLog entity = this.getById(id);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "装配步骤日志不存在");
        }
        entity.setDeleted(1);
        this.updateById(entity);
    }

    private AssemblyStepLogVO toVO(AssemblyStepLog entity) {
        AssemblyStepLogVO vo = new AssemblyStepLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
