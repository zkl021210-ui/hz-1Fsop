package com.sop.process.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 产品型号操作命令
 *
 * @author SOP Team
 */
@Data
public class ProductModelCommand {

    /** 型号编码 */
    @NotBlank(message = "型号编码不能为空")
    private String modelCode;

    /** 型号名称 */
    @NotBlank(message = "型号名称不能为空")
    private String modelName;

    /** 型号描述 */
    private String description;
}
