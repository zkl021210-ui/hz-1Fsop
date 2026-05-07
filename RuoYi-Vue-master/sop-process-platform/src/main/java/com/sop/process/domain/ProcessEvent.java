package com.sop.process.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程事件实体
 * <p>
 * 对应数据库表 process_event，记录装配流程中的关键事件，
 * 支持 eventId 幂等校验。
 *
 * @author SOP Team
 */
@Data
@TableName("process_event")
public class ProcessEvent {

    /** 事件ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 外部事件ID（Python回调eventId或自动生成的LEGACY_ID） */
    private String eventId;

    /** 关联任务ID */
    private Long taskId;

    /** 设备编码 */
    private String deviceSn;

    /** 步骤ID */
    private Long stepId;

    /** 步骤日志ID */
    private Long stepRunId;

    /** 事件类型：AI_PASS_RECEIVED / TASK_COMPLETED / CALLBACK_REJECTED / VISION_SERVICE_ERROR */
    private String eventType;

    /** 事件状态：RECEIVED / PROCESSING / SUCCESS / FAILED / IGNORED */
    private String status;

    /** 原始载荷（JSON） */
    private String payload;

    /** 处理结果简述 */
    private String result;

    /** 异常信息（status=FAILED时） */
    private String errorMessage;

    /** 处理完成时间 */
    private LocalDateTime processedTime;

    /** 重复回调次数 */
    private Integer duplicateCount;

    /** 最后一次收到重复回调的时间 */
    private LocalDateTime lastReceivedTime;

    /** MQ 重试次数 */
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetry;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
