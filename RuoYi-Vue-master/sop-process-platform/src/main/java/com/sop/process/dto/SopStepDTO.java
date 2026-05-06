package com.sop.process.dto;

import lombok.Data;

/**
 * SOP 步骤 DTO
 * <p>
 * 用于新增/修改 SOP 步骤时的请求参数传输。
 *
 * @author SOP Team
 */
@Data
public class SopStepDTO {

    /** 关联型号编码 */
    private String modelCode;

    /** 作业阶段 */
    private String processStage;

    /** 步骤顺序 */
    private Integer stepOrder;

    /** 步骤标题 */
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
