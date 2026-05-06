package com.sop.process.vo;

import lombok.Data;

/**
 * 启动步骤结果 VO
 *
 * @author SOP Team
 */
@Data
public class StartStepResultVO {

    /** 任务ID */
    private Long taskId;

    /** 步骤日志ID */
    private Long stepLogId;

    /** 步骤ID */
    private Long stepId;

    /** 步骤序号 */
    private Integer stepNo;

    /** 步骤名称 */
    private String stepName;

    /** 步骤状态 */
    private String status;

    /** 检测目标标签 */
    private String targetLabel;

    /** 下一步检测目标标签 */
    private String nextTargetLabel;

    /** 录像路径 */
    private String videoPath;
}
