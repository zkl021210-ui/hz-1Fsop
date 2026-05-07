package com.sop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SOP 流程平台启动类
 * <p>
 * 工业标准作业流程（Standard Operating Procedure）平台后端服务入口。
 *
 * @author SOP Team
 */
@SpringBootApplication
@MapperScan("com.sop.**.mapper")
@EnableScheduling
public class SopApplication {

    public static void main(String[] args) {
        SpringApplication.run(SopApplication.class, args);
    }
}
