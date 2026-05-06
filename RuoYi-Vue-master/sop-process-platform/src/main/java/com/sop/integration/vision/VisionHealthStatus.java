package com.sop.integration.vision;

import lombok.Data;

/**
 * 视觉服务健康检查状态
 *
 * @author SOP Team
 */
@Data
public class VisionHealthStatus {

    /** 服务是否可用 */
    private boolean available;

    /** 服务版本 */
    private String version;

    /** 检查时间 */
    private String checkTime;
}
