package com.sop.process.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 装配任务 VO
 * <p>
 * 用于返回给前端的装配任务视图数据。
 *
 * @author SOP Team
 */
@Data
public class AssemblyTaskVO {

    private Long taskId;
    private String modelCode;
    private String deviceSn;
    private Long workerId;
    private String workerName;
    private Integer currentStepIndex;
    private String status;
    private Integer assemblyRound;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
