package com.sop.process.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * SOP 步骤 VO
 * <p>
 * 用于返回给前端的 SOP 步骤视图数据。
 *
 * @author SOP Team
 */
@Data
public class SopStepVO {

    private Long stepId;
    private String modelCode;
    private String processStage;
    private Integer stepOrder;
    private String stepTitle;
    private String stepDesc;
    private String imageUrl;
    private String detectTarget;
    private String roiConfig;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
