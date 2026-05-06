package com.sop.process.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 工人操作命令
 *
 * @author SOP Team
 */
@Data
public class WorkerCommand {

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    private String workerName;

    /** 工号 */
    @NotBlank(message = "工号不能为空")
    private String workerCode;

    /** 班组 */
    private String teamName;

    /** 状态（0-正常 1-停用） */
    private String status;
}
