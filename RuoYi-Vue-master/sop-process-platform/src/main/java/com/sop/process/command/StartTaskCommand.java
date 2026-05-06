package com.sop.process.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开工命令
 *
 * @author SOP Team
 */
@Data
public class StartTaskCommand {

    /** 设备编码 */
    @NotBlank(message = "设备编码不能为空")
    private String deviceSn;

    /** 操作工ID */
    @NotNull(message = "操作工ID不能为空")
    private Long workerId;

    /** 型号编码 */
    @NotBlank(message = "型号编码不能为空")
    private String modelCode;
}
