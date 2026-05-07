package com.sop.process.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 装配任务实体
 * <p>
 * 对应数据库表 assembly_task，记录每次装配作业的主任务信息，
 * 包括关联型号、设备编码、操作工、当前进度和状态。
 *
 * @author SOP Team
 */
@Data
@TableName("assembly_task")
public class AssemblyTask {

    /** 任务ID */
    @TableId(type = IdType.AUTO)
    private Long taskId;

    /** 关联型号编码 */
    private String modelCode;

    /** 设备编码（转辙机编码） */
    private String deviceSn;

    /** 操作工ID */
    private Long workerId;

    /** 操作工姓名 */
    private String workerName;

    /** 当前步骤序号（对应 SopStep.stepOrder，与 stepOrder 直接等值比较） */
    private Integer currentStepIndex;

    /** 任务状态（0-进行中 1-暂停 2-已完成 3-异常） */
    private String status;

    /** 装配轮次 */
    private Integer assemblyRound;

    /** 任务开始时间 */
    private LocalDateTime startTime;

    /** 任务完成时间 */
    private LocalDateTime finishTime;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

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
