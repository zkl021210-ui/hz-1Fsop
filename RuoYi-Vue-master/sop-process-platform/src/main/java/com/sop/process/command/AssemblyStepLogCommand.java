package com.sop.process.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 装配步骤日志操作命令
 *
 * @author SOP Team
 */
@Data
public class AssemblyStepLogCommand {

    /** 关联任务ID */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    /** 关联步骤ID */
    @NotNull(message = "步骤ID不能为空")
    private Long stepId;

    /** 设备编号 */
    @NotBlank(message = "设备编号不能为空")
    private String serialNumber;

    /** 型号编码 */
    @NotBlank(message = "型号编码不能为空")
    private String modelCode;

    /** 装配轮次 */
    private Integer assemblyRound;

    /** 作业阶段 */
    private String processStage;

    /** 工人姓名 */
    private String workerName;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 步骤状态 */
    private String status;

    /** 视频录制地址 */
    private String videoUrl;
}
