package com.sop.process.command;

import lombok.Data;

/**
 * AI 自动过站回调命令
 * <p>
 * 兼容新旧两种 Python 视觉服务回调格式。
 *
 * @author SOP Team
 */
@Data
public class AiNextStepCallbackCommand {

    /** 事件ID（新格式） */
    private String eventId;

    /** 设备编码 */
    private String deviceSn;

    /** 任务ID（新格式，可为空） */
    private Long taskId;

    /** 步骤ID（新格式，可为空） */
    private Long stepId;

    /** 步骤执行日志ID（新格式，可为空） */
    private Long stepRunId;

    /** 动作类型 */
    private String action;

    /** 状态 */
    private String status;
}
