package com.sop.integration.vision;

import lombok.Builder;
import lombok.Data;

/**
 * 启动录像命令
 *
 * @author SOP Team
 */
@Data
@Builder
public class StartRecordCommand {

    /** 设备编码 */
    private String deviceSn;

    /** 步骤ID */
    private Long stepId;

    /** 任务ID */
    private Long taskId;

    /** 步骤日志ID */
    private Long stepRunId;
}
