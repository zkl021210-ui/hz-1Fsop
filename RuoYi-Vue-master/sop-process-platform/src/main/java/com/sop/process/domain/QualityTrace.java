package com.sop.process.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 质量追溯实体
 * <p>
 * 对应数据库表 quality_trace，记录每次装配任务完成后的基础质量追溯信息。
 *
 * @author SOP Team
 */
@Data
@TableName("quality_trace")
public class QualityTrace {

    /** 追溯ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联任务ID */
    private Long taskId;

    /** 设备编码 */
    private String deviceSn;

    /** 型号编码 */
    private String modelCode;

    /** 操作工ID */
    private Long workerId;

    /** 总步骤数 */
    private Integer totalSteps;

    /** AI通过步骤数 */
    private Integer passedSteps;

    /** 手动通过步骤数 */
    private Integer manualSteps;

    /** 异常步骤数 */
    private Integer exceptionCount;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 完成时间 */
    private LocalDateTime finishTime;

    /** 总耗时（秒） */
    private Long totalDuration;

    /** 最终结果（PASS / FAIL / MANUAL） */
    private String finalResult;

    /** 是否删除（0-正常 1-删除） */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
