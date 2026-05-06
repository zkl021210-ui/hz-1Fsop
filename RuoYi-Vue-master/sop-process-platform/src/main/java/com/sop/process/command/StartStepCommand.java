package com.sop.process.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 启动步骤命令
 *
 * @author SOP Team
 */
@Data
public class StartStepCommand {

    /** 任务ID */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    /** 步骤ID */
    @NotNull(message = "步骤ID不能为空")
    private Long stepId;
}
