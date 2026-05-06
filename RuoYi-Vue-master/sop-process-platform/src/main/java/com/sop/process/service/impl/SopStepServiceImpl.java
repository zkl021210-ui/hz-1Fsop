package com.sop.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sop.common.exception.BusinessException;
import com.sop.common.exception.ErrorCode;
import com.sop.process.domain.SopStep;
import com.sop.process.dto.SopStepDTO;
import com.sop.process.mapper.SopStepMapper;
import com.sop.process.service.SopStepService;
import com.sop.process.vo.SopStepVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SopStepServiceImpl extends ServiceImpl<SopStepMapper, SopStep> implements SopStepService {

    @Override
    public Page<SopStepVO> pageSopSteps(int page, int pageSize, String modelCode, String processStage) {
        Page<SopStep> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<SopStep> wrapper = new LambdaQueryWrapper<SopStep>()
                .eq(SopStep::getDeleted, 0)
                .eq(modelCode != null && !modelCode.isEmpty(), SopStep::getModelCode, modelCode)
                .eq(processStage != null && !processStage.isEmpty(), SopStep::getProcessStage, processStage)
                .orderByAsc(SopStep::getStepOrder);
        Page<SopStep> result = this.page(pageParam, wrapper);
        Page<SopStepVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public List<SopStepVO> listByModelCode(String modelCode) {
        LambdaQueryWrapper<SopStep> wrapper = new LambdaQueryWrapper<SopStep>()
                .eq(SopStep::getDeleted, 0)
                .eq(SopStep::getModelCode, modelCode)
                .orderByAsc(SopStep::getStepOrder);
        return this.list(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public SopStepVO getSopStepById(Long stepId) {
        SopStep entity = this.getById(stepId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "SOP步骤不存在");
        }
        return toVO(entity);
    }

    @Override
    public SopStepVO createSopStep(SopStepDTO dto) {
        SopStep entity = new SopStep();
        BeanUtils.copyProperties(dto, entity);
        this.save(entity);
        return toVO(entity);
    }

    @Override
    public SopStepVO updateSopStep(Long stepId, SopStepDTO dto) {
        SopStep entity = this.getById(stepId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "SOP步骤不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setStepId(stepId);
        this.updateById(entity);
        return toVO(entity);
    }

    @Override
    public void deleteSopStep(Long stepId) {
        SopStep entity = this.getById(stepId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "SOP步骤不存在");
        }
        entity.setDeleted(1);
        this.updateById(entity);
    }

    private SopStepVO toVO(SopStep entity) {
        SopStepVO vo = new SopStepVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
