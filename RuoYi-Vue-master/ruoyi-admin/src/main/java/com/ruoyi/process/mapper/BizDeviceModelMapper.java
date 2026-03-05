package com.ruoyi.process.mapper;

import java.util.List;
import com.ruoyi.process.domain.BizDeviceModel;

/**
 * 转辙机型号Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-23
 */
public interface BizDeviceModelMapper 
{
    /**
     * 查询转辙机型号
     * 
     * @param modelId 转辙机型号主键
     * @return 转辙机型号
     */
    public BizDeviceModel selectBizDeviceModelByModelId(Long modelId);

    /**
     * 查询转辙机型号列表
     * 
     * @param bizDeviceModel 转辙机型号
     * @return 转辙机型号集合
     */
    public List<BizDeviceModel> selectBizDeviceModelList(BizDeviceModel bizDeviceModel);

    /**
     * 新增转辙机型号
     * 
     * @param bizDeviceModel 转辙机型号
     * @return 结果
     */
    public int insertBizDeviceModel(BizDeviceModel bizDeviceModel);

    /**
     * 修改转辙机型号
     * 
     * @param bizDeviceModel 转辙机型号
     * @return 结果
     */
    public int updateBizDeviceModel(BizDeviceModel bizDeviceModel);

    /**
     * 删除转辙机型号
     * 
     * @param modelId 转辙机型号主键
     * @return 结果
     */
    public int deleteBizDeviceModelByModelId(Long modelId);

    /**
     * 批量删除转辙机型号
     * 
     * @param modelIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizDeviceModelByModelIds(Long[] modelIds);
}
