package com.sop.integration.vision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 停止录像命令
 *
 * @author SOP Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopRecordCommand {

    /** 设备编码 */
    private String deviceSn;

    /** 任务ID */
    private Long taskId;

    /** 步骤日志ID */
    private Long stepRunId;
}
