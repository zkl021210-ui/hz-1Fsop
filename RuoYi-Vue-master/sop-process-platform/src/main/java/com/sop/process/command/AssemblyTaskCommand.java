package com.sop.process.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 装配任务操作命令
 *
 * @author SOP Team
 */
@Data
public class AssemblyTaskCommand {

    /** 关联型号编码 */
    @NotBlank(message = "型号编码不能为空")
    private String modelCode;

    /** 设备编码 */
    @NotBlank(message = "设备编码不能为空")
    private String deviceSn;

    /** 操作工ID */
    @NotNull(message = "操作工ID不能为空")
    private Long workerId;

    /** 操作工姓名 */
    @NotBlank(message = "操作工姓名不能为空")
    private String workerName;

    /** 当前步骤索引 */
    private Integer currentStepIndex;

    /** 任务状态 */
    private String status;

    /** 装配轮次 */
    private Integer assemblyRound;
}
