package com.sop.process.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 装配步骤日志实体
 * <p>
 * 对应数据库表 assembly_step_log，记录装配任务中每个步骤的执行流水，
 * 包括开始/结束时间、视频记录、检测结果等。
 *
 * @author SOP Team
 */
@Data
@TableName("assembly_step_log")
public class AssemblyStepLog {

    /** 日志ID */
    @TableId(type = IdType.AUTO)
    private Long id;

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

    /** 步骤状态（0-待执行 1-执行中 2-已完成 3-异常） */
    private String status;

    /** 通过方式（AI_PASS / MANUAL_PASS） */
    private String passType;

    /** 步骤耗时（秒） */
    private Long duration;

    /** 步骤序号（对应 SopStep.stepOrder） */
    private Integer stepNo;

    /** 视频录制地址 */
    private String videoUrl;

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
