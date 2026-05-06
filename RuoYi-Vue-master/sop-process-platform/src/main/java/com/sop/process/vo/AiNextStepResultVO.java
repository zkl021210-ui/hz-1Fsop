package com.sop.process.vo;

import lombok.Data;

/**
 * AI 过站回调结果 VO
 *
 * @author SOP Team
 */
@Data
public class AiNextStepResultVO {

    /** 任务ID */
    private Long taskId;

    /** 设备编码 */
    private String deviceSn;

    /** 任务状态 */
    private String taskStatus;

    /** 当前步骤序号 */
    private Integer currentStepNo;

    /** 是否有下一步 */
    private Boolean hasNextStep;

    /** 下一步骤ID */
    private Long nextStepId;

    /** 下一步骤名称 */
    private String nextStepName;

    /** 已完成步骤日志ID */
    private Long completedStepLogId;
}
