package com.sop.process.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 手动完成任务结果 VO
 *
 * @author SOP Team
 */
@Data
public class FinishTaskResultVO {

    /** 任务ID */
    private Long taskId;

    /** 设备编码 */
    private String deviceSn;

    /** 任务状态 */
    private String status;

    /** 完成时间 */
    private LocalDateTime finishTime;

    /** 总耗时（秒） */
    private Long totalDuration;
}
