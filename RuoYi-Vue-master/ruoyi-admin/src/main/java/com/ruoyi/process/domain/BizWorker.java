package com.ruoyi.process.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工人信息对象 biz_worker
 *
 */
public class BizWorker extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long workerId;

    /** 姓名 */
    @Excel(name = "姓名")
    private String workerName;

    /** 工号 */
    @Excel(name = "工号")
    private String workerCode;

    /** 班组 */
    @Excel(name = "班组")
    private String teamName;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态(0正常 1停用)")
    private String status;

    public void setWorkerId(Long workerId) 
    {
        this.workerId = workerId;
    }

    public Long getWorkerId() 
    {
        return workerId;
    }

    public void setWorkerName(String workerName) 
    {
        this.workerName = workerName;
    }

    public String getWorkerName() 
    {
        return workerName;
    }

    public void setWorkerCode(String workerCode) 
    {
        this.workerCode = workerCode;
    }

    public String getWorkerCode() 
    {
        return workerCode;
    }

    public void setTeamName(String teamName) 
    {
        this.teamName = teamName;
    }

    public String getTeamName() 
    {
        return teamName;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("workerId", getWorkerId())
            .append("workerName", getWorkerName())
            .append("workerCode", getWorkerCode())
            .append("teamName", getTeamName())
            .append("status", getStatus())
            .toString();
    }
}
