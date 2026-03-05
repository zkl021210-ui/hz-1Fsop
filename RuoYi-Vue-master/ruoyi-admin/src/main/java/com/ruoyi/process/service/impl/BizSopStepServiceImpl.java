package com.ruoyi.process.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.process.mapper.BizSopStepMapper;
import com.ruoyi.process.domain.BizSopStep;
import com.ruoyi.process.service.IBizSopStepService;

/**
 * SOP步骤配置Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-23
 */
@Service
public class BizSopStepServiceImpl implements IBizSopStepService 
{
    @Autowired
    private BizSopStepMapper bizSopStepMapper;

    /**
     * 查询SOP步骤配置
     * 
     * @param stepId SOP步骤配置主键
     * @return SOP步骤配置
     */
    @Override
    public BizSopStep selectBizSopStepByStepId(Long stepId)
    {
        return bizSopStepMapper.selectBizSopStepByStepId(stepId);
    }

    /**
     * 查询SOP步骤配置列表
     * 
     * @param bizSopStep SOP步骤配置
     * @return SOP步骤配置
     */
    @Override
    public List<BizSopStep> selectBizSopStepList(BizSopStep bizSopStep)
    {
        return bizSopStepMapper.selectBizSopStepList(bizSopStep);
    }

    /**
     * 新增SOP步骤配置
     * 
     * @param bizSopStep SOP步骤配置
     * @return 结果
     */
    @Override
    public int insertBizSopStep(BizSopStep bizSopStep)
    {
        return bizSopStepMapper.insertBizSopStep(bizSopStep);
    }

    /**
     * 修改SOP步骤配置
     * 
     * @param bizSopStep SOP步骤配置
     * @return 结果
     */
    @Override
    public int updateBizSopStep(BizSopStep bizSopStep)
    {
        return bizSopStepMapper.updateBizSopStep(bizSopStep);
    }

    /**
     * 批量删除SOP步骤配置
     * 
     * @param stepIds 需要删除的SOP步骤配置主键
     * @return 结果
     */
    @Override
    public int deleteBizSopStepByStepIds(Long[] stepIds)
    {
        return bizSopStepMapper.deleteBizSopStepByStepIds(stepIds);
    }

    /**
     * 删除SOP步骤配置信息
     * 
     * @param stepId SOP步骤配置主键
     * @return 结果
     */
    @Override
    public int deleteBizSopStepByStepId(Long stepId)
    {
        return bizSopStepMapper.deleteBizSopStepByStepId(stepId);
    }
}
