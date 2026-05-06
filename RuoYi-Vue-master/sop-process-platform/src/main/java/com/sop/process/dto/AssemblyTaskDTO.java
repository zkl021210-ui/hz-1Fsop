package com.sop.process.dto;

import lombok.Data;

/**
 * 装配任务 DTO
 * <p>
 * 用于新增/修改装配任务时的请求参数传输。
 *
 * @author SOP Team
 */
@Data
public class AssemblyTaskDTO {

    /** 关联型号编码 */
    private String modelCode;

    /** 设备编码 */
    private String deviceSn;

    /** 操作工ID */
    private Long workerId;

    /** 操作工姓名 */
    private String workerName;

    /** 当前步骤索引 */
    private Integer currentStepIndex;

    /** 任务状态 */
    private String status;

    /** 装配轮次 */
    private Integer assemblyRound;
}
