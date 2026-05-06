package com.sop.process.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工人实体
 * <p>
 * 对应数据库表 worker，存储操作工基本信息。
 *
 * @author SOP Team
 */
@Data
@TableName("worker")
public class Worker {

    /** 工人ID */
    @TableId(type = IdType.AUTO)
    private Long workerId;

    /** 姓名 */
    private String workerName;

    /** 工号 */
    private String workerCode;

    /** 班组 */
    private String teamName;

    /** 状态（0-正常 1-停用） */
    private String status;

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
