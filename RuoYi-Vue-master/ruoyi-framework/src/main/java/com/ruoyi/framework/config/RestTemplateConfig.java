package com.ruoyi.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置类
 * 
 * 用于在整个应用中提供一个可注入的 RestTemplate 实例，
 * 以便与其他微服务或外部API（如Python服务）进行HTTP通信。
 *
 * @author RuoYi-AI
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
