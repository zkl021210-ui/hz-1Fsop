package com.sop.process.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SOP 步骤实体
 * <p>
 * 对应数据库表 sop_step，存储标准作业流程中每个步骤的配置信息，
 * 包括操作指导、视觉检测目标、ROI 区域等。
 *
 * @author SOP Team
 */
@Data
@TableName("sop_step")
public class SopStep {

    /** 步骤ID */
    @TableId(type = IdType.AUTO)
    private Long stepId;

    /** 关联型号编码 */
    private String modelCode;

    /** 作业阶段 */
    private String processStage;

    /** 步骤顺序 */
    private Integer stepOrder;

    /** 步骤标题 */
    private String stepTitle;

    /** 操作指导描述 */
    private String stepDesc;

    /** 示意图路径 */
    private String imageUrl;

    /** YOLO 检测目标（JSON 格式，如 ["screw","nut"]） */
    private String detectTarget;

    /** ROI 相对坐标配置（JSON 格式） */
    private String roiConfig;

    /** 检测目标标签（如 "screw"） */
    private String targetLabel;

    /** 下一步检测目标标签 */
    private String nextTargetLabel;

    /** 标准工时（秒） */
    private Integer standardDuration;

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
