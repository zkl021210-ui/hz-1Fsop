package com.ruoyi.process.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 转辙机型号对象 biz_device_model
 */
public class BizDeviceModel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long modelId;

    /** 型号名称 */
    @Excel(name = "型号名称")
    private String modelName;

    /** 备注 */
    @Excel(name = "备注")
    private String description;

    public void setModelId(Long modelId) 
    {
        this.modelId = modelId;
    }

    public Long getModelId() 
    {
        return modelId;
    }

    public void setModelName(String modelName) 
    {
        this.modelName = modelName;
    }

    public String getModelName() 
    {
        return modelName;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("modelId", getModelId())
            .append("modelName", getModelName())
            .append("description", getDescription())
            .toString();
    }
}
