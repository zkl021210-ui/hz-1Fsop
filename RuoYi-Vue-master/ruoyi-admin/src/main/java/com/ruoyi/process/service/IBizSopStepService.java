package com.ruoyi.process.service;

import java.util.List;
import com.ruoyi.process.domain.BizSopStep;

/**
 * SOP步骤配置Service接口
 */
public interface IBizSopStepService 
{
    /**
     * 查询SOP步骤配置
     * 
     * @param stepId SOP步骤配置主键
     * @return SOP步骤配置
     */
    public BizSopStep selectBizSopStepByStepId(Long stepId);

    /**
     * 查询SOP步骤配置列表
     * 
     * @param bizSopStep SOP步骤配置
     * @return SOP步骤配置集合
     */
    public List<BizSopStep> selectBizSopStepList(BizSopStep bizSopStep);

    /**
     * 新增SOP步骤配置
     * 
     * @param bizSopStep SOP步骤配置
     * @return 结果
     */
    public int insertBizSopStep(BizSopStep bizSopStep);

    /**
     * 修改SOP步骤配置
     * 
     * @param bizSopStep SOP步骤配置
     * @return 结果
     */
    public int updateBizSopStep(BizSopStep bizSopStep);

    /**
     * 批量删除SOP步骤配置
     * 
     * @param stepIds 需要删除的SOP步骤配置主键集合
     * @return 结果
     */
    public int deleteBizSopStepByStepIds(Long[] stepIds);

    /**
     * 删除SOP步骤配置信息
     * 
     * @param stepId SOP步骤配置主键
     * @return 结果
     */
    public int deleteBizSopStepByStepId(Long stepId);
}
