package com.sop.integration.vision;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 视觉服务下发步骤配置命令
 *
 * @author SOP Team
 */
@Data
@Builder
public class VisionStepConfigCommand {

    /** 设备编码 */
    private String deviceSn;

    /** 步骤ID */
    private Long stepId;

    /** 当前检测目标 */
    private String target;

    /** 下一步检测目标 */
    private String nextTarget;

    /** 型号编码 */
    private String modelCode;

    /** 历史已完成的检测目标列表 */
    private List<String> historyTargets;

    /** 时间参数1 */
    private Integer time1;

    /** 时间参数2 */
    private Integer time2;
}
