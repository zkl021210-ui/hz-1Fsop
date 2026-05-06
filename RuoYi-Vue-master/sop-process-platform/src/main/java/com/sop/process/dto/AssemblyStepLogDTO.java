package com.sop.process.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 装配步骤日志 DTO
 * <p>
 * 用于新增/修改装配步骤日志时的请求参数传输。
 *
 * @author SOP Team
 */
@Data
public class AssemblyStepLogDTO {

    /** 关联任务ID */
    private Long taskId;

    /** 关联步骤ID */
    private Long stepId;

    /** 设备编号 */
    private String serialNumber;

    /** 型号编码 */
    private String modelCode;

    /** 装配轮次 */
    private Integer assemblyRound;

    /** 作业阶段 */
    private String processStage;

    /** 工人姓名 */
    private String workerName;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 步骤状态 */
    private String status;

    /** 视频录制地址 */
    private String videoUrl;
}
