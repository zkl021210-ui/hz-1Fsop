package com.sop.process.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sop.process.dto.SopStepDTO;
import com.sop.process.vo.SopStepVO;

import java.util.List;

/**
 * SOP 步骤 Service 接口
 *
 * @author SOP Team
 */
public interface SopStepService {

    /**
     * 分页查询 SOP 步骤
     */
    Page<SopStepVO> pageSopSteps(int page, int pageSize, String modelCode, String processStage);

    /**
     * 根据型号编码查询步骤列表
     */
    List<SopStepVO> listByModelCode(String modelCode);

    /**
     * 根据ID查询 SOP 步骤
     */
    SopStepVO getSopStepById(Long stepId);

    /**
     * 新增 SOP 步骤
     */
    SopStepVO createSopStep(SopStepDTO dto);

    /**
     * 修改 SOP 步骤
     */
    SopStepVO updateSopStep(Long stepId, SopStepDTO dto);

    /**
     * 删除 SOP 步骤
     */
    void deleteSopStep(Long stepId);
}
