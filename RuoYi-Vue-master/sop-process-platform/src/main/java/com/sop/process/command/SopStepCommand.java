package com.sop.process.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * SOP 步骤操作命令
 *
 * @author SOP Team
 */
@Data
public class SopStepCommand {

    /** 关联型号编码 */
    @NotBlank(message = "型号编码不能为空")
    private String modelCode;

    /** 作业阶段 */
    @NotBlank(message = "作业阶段不能为空")
    private String processStage;

    /** 步骤顺序 */
    @NotNull(message = "步骤顺序不能为空")
    private Integer stepOrder;

    /** 步骤标题 */
    @NotBlank(message = "步骤标题不能为空")
    private String stepTitle;

    /** 操作指导描述 */
    private String stepDesc;

    /** 示意图路径 */
    private String imageUrl;

    /** YOLO 检测目标 */
    private String detectTarget;

    /** ROI 相对坐标配置 */
    private String roiConfig;
}
