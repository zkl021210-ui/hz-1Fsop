package com.ruoyi.process.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * SOP步骤配置对象 biz_sop_step
 *
 */
public class BizSopStep extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long stepId;

    /** 关联型号 */
    @Excel(name = "关联型号")
    private String modelCode;

    /** 作业阶段 */
    @Excel(name = "作业阶段")
    private String processStage;

    /** 步骤顺序 */
    @Excel(name = "步骤顺序")
    private Long stepOrder;

    /** 步骤标题 */
    @Excel(name = "步骤标题")
    private String stepTitle;

    /** 操作指导 */
    @Excel(name = "操作指导")
    private String stepDesc;

    /** 示意图路径 */
    @Excel(name = "示意图路径")
    private String imageUrl;

    /** YOLO检测目标 */
    @Excel(name = "YOLO检测目标")
    private String detectTarget;

    /** ROI相对坐标 */
    @Excel(name = "ROI相对坐标")
    private String roiConfig;


    public void setStepId(Long stepId) 
    {
        this.stepId = stepId;
    }

    public Long getStepId() 
    {
        return stepId;
    }

    public void setModelCode(String modelCode)
    {
        this.modelCode = modelCode;
    }

    public String getModelCode()
    {
        return modelCode;
    }

    public void setProcessStage(String processStage) 
    {
        this.processStage = processStage;
    }

    public String getProcessStage() 
    {
        return processStage;
    }

    public void setStepOrder(Long stepOrder) 
    {
        this.stepOrder = stepOrder;
    }

    public Long getStepOrder() 
    {
        return stepOrder;
    }

    public void setStepTitle(String stepTitle) 
    {
        this.stepTitle = stepTitle;
    }

    public String getStepTitle() 
    {
        return stepTitle;
    }

    public void setStepDesc(String stepDesc) 
    {
        this.stepDesc = stepDesc;
    }

    public String getStepDesc() 
    {
        return stepDesc;
    }

    public void setImageUrl(String imageUrl) 
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() 
    {
        return imageUrl;
    }

    public void setDetectTarget(String detectTarget) 
    {
        this.detectTarget = detectTarget;
    }

    public String getDetectTarget() 
    {
        return detectTarget;
    }

    public void setRoiConfig(String roiConfig) 
    {
        this.roiConfig = roiConfig;
    }

    public String getRoiConfig() 
    {
        return roiConfig;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("stepId", getStepId())
            .append("modelCode", getModelCode())
            .append("processStage", getProcessStage())
            .append("stepOrder", getStepOrder())
            .append("stepTitle", getStepTitle())
            .append("stepDesc", getStepDesc())
            .append("imageUrl", getImageUrl())
            .append("detectTarget", getDetectTarget())
            .append("roiConfig", getRoiConfig())
            .toString();
    }
}
