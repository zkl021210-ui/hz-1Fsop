package com.ruoyi.process.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 装配流水日志对象 biz_assembly_log
 */
public class BizAssemblyLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long stepId; // 确保stepId存在
    private String videoUrl;

    @Excel(name = "设备编号")
    private String serialNumber;

    @Excel(name = "型号")
    private String modelCode;

    @Excel(name = "装配轮次")
    private Integer assemblyRound;

    @Excel(name = "作业阶段")
    private String processStage;

    @Excel(name = "工人姓名")
    private String workerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @Excel(name = "状态")
    private String status;

    // === Getters & Setters ===

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }

    public void setStepId(Long stepId) { this.stepId = stepId; }
    public Long getStepId() { return stepId; }

    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getVideoUrl() { return videoUrl; }

    public void setAssemblyRound(Integer assemblyRound) { this.assemblyRound = assemblyRound; }
    public Integer getAssemblyRound() { return assemblyRound; }

    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getSerialNumber() { return serialNumber; }

    public void setModelCode(String modelCode) { this.modelCode = modelCode; }
    public String getModelCode() { return modelCode; }

    public void setProcessStage(String processStage) { this.processStage = processStage; }
    public String getProcessStage() { return processStage; }

    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getWorkerName() { return workerName; }

    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getStartTime() { return startTime; }

    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public Date getEndTime() { return endTime; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("serialNumber", getSerialNumber())
                .append("processStage", getProcessStage())
                .append("videoUrl", getVideoUrl())
                .toString();
    }
}
