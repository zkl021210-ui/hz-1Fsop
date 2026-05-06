package com.sop.process.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 装配步骤日志 VO
 * <p>
 * 用于返回给前端的装配步骤日志视图数据。
 *
 * @author SOP Team
 */
@Data
public class AssemblyStepLogVO {

    private Long id;
    private Long taskId;
    private Long stepId;
    private String serialNumber;
    private String modelCode;
    private Integer assemblyRound;
    private String processStage;
    private String workerName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String videoUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
