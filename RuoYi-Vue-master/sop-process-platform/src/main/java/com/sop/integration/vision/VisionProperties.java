package com.sop.integration.vision;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 视觉服务配置属性
 * <p>
 * 绑定 application.yml 中 vision.service.* 配置项。
 *
 * @author SOP Team
 */
@Data
@Component
@ConfigurationProperties(prefix = "vision.service")
public class VisionProperties {

    /** 是否启用视觉服务（false 时所有 VisionGateway 调用直接返回成功，用于本地纯 Java 测试） */
    private boolean enabled = false;

    /** 视觉服务基础地址 */
    private String baseUrl = "http://localhost:5000";

    /** 连接超时（毫秒） */
    private int connectTimeout = 5000;

    /** 读取超时（毫秒） */
    private int readTimeout = 30000;
}
