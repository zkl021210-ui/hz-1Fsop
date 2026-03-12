package com.ruoyi.process.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 装配任务主对象 biz_assembly_task
 */
public class BizAssemblyTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务主键 */
    private Long taskId;

    /** 转辙机型号 (Java属性 modelCode 对应 数据库列 device_type) */
    @Excel(name = "转辙机型号")
    private String modelCode;

    /** 转辙机编码 */
    @Excel(name = "转辙机编码")
    private String deviceSn;

    /** 操作工ID */
    private Long workerId;

    /** 操作工姓名 */
    @Excel(name = "操作工姓名")
    private String workerName;

    /** 当前步骤 */
    @Excel(name = "当前步骤")
    private Integer currentStepIndex;

    /** 状态 (0=进行中 1=暂停 2=已完成 3=异常) */
    @Excel(name = "状态", readConverterExp = "0=进行中,1=暂停,2=已完成,3=异常")
    private String status;

    /** 装配轮次 (新增字段) */
    @Excel(name = "装配轮次")
    private Integer assemblyRound;

    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getTaskId() { return taskId; }

    public void setModelCode(String modelCode)
    {
        this.modelCode = modelCode;
    }
    public String getModelCode()
    {
        return modelCode;
    }

    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn; }
    public String getDeviceSn() { return deviceSn; }

    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public Long getWorkerId() { return workerId; }

    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getWorkerName() { return workerName; }

    public void setCurrentStepIndex(Integer currentStepIndex) { this.currentStepIndex = currentStepIndex; }
    public Integer getCurrentStepIndex() { return currentStepIndex; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setAssemblyRound(Integer assemblyRound) { this.assemblyRound = assemblyRound; }
    public Integer getAssemblyRound() { return assemblyRound; }


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("taskId", getTaskId())
                .append("modelCode", getModelCode()) // 把型号也打印出来方便调试
                .append("deviceSn", getDeviceSn())
                .append("status", getStatus())
                .append("assemblyRound", getAssemblyRound())
                .toString();
    }
}
