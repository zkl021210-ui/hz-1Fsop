package com.sop.process.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 手动完成任务命令
 *
 * @author SOP Team
 */
@Data
public class FinishTaskCommand {

    /** 任务ID */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    /** 完成原因 */
    private String reason;
}
