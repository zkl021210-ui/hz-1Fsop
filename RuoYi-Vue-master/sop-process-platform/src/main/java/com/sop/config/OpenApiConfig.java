package com.sop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Knife4j) 配置
 * <p>
 * 配置 API 文档信息，访问地址：http://localhost:port/doc.html
 *
 * @author SOP Team
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SOP 流程平台 API 文档")
                        .description("工业标准作业流程（Standard Operating Procedure）平台后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SOP Team")
                                .email("sop@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
