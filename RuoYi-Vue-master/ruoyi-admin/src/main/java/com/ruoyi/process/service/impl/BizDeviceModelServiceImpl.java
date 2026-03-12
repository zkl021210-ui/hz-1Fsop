package com.ruoyi.process.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.process.mapper.BizDeviceModelMapper;
import com.ruoyi.process.domain.BizDeviceModel;
import com.ruoyi.process.service.IBizDeviceModelService;

/**
 * 转辙机型号Service业务层处理
 */
@Service
public class BizDeviceModelServiceImpl implements IBizDeviceModelService 
{
    @Autowired
    private BizDeviceModelMapper bizDeviceModelMapper;

    /**
     * 查询转辙机型号
     * 
     * @param modelId 转辙机型号主键
     * @return 转辙机型号
     */
    @Override
    public BizDeviceModel selectBizDeviceModelByModelId(Long modelId)
    {
        return bizDeviceModelMapper.selectBizDeviceModelByModelId(modelId);
    }

    /**
     * 查询转辙机型号列表
     * 
     * @param bizDeviceModel 转辙机型号
     * @return 转辙机型号
     */
    @Override
    public List<BizDeviceModel> selectBizDeviceModelList(BizDeviceModel bizDeviceModel)
    {
        return bizDeviceModelMapper.selectBizDeviceModelList(bizDeviceModel);
    }

    /**
     * 新增转辙机型号
     * 
     * @param bizDeviceModel 转辙机型号
     * @return 结果
     */
    @Override
    public int insertBizDeviceModel(BizDeviceModel bizDeviceModel)
    {
        return bizDeviceModelMapper.insertBizDeviceModel(bizDeviceModel);
    }

    /**
     * 修改转辙机型号
     * 
     * @param bizDeviceModel 转辙机型号
     * @return 结果
     */
    @Override
    public int updateBizDeviceModel(BizDeviceModel bizDeviceModel)
    {
        return bizDeviceModelMapper.updateBizDeviceModel(bizDeviceModel);
    }

    /**
     * 批量删除转辙机型号
     * 
     * @param modelIds 需要删除的转辙机型号主键
     * @return 结果
     */
    @Override
    public int deleteBizDeviceModelByModelIds(Long[] modelIds)
    {
        return bizDeviceModelMapper.deleteBizDeviceModelByModelIds(modelIds);
    }

    /**
     * 删除转辙机型号信息
     * 
     * @param modelId 转辙机型号主键
     * @return 结果
     */
    @Override
    public int deleteBizDeviceModelByModelId(Long modelId)
    {
        return bizDeviceModelMapper.deleteBizDeviceModelByModelId(modelId);
    }
}
