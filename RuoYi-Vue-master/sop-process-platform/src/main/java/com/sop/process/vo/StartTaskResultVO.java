package com.sop.process.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 开工结果 VO
 *
 * @author SOP Team
 */
@Data
public class StartTaskResultVO {

    /** 任务ID */
    private Long taskId;

    /** 设备编码 */
    private String deviceSn;

    /** 型号编码 */
    private String modelCode;

    /** 当前步骤序号 */
    private Integer currentStepNo;

    /** 任务状态 */
    private String status;

    /** 开工时间 */
    private LocalDateTime startTime;
}
