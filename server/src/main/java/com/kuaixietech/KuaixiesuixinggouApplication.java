package com.kuaixietech;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Java 21 / Spring Boot 4 entrypoint; scans all business domains. */
@SpringBootApplication(scanBasePackages = "com.kuaixietech")
@MapperScan(basePackages = "com.kuaixietech.mapper")
@MapperScan("com.kuaixietech.market")
public class KuaixiesuixinggouApplication {
    public static void main(String[] args) {
        SpringApplication.run(KuaixiesuixinggouApplication.class, args);
    }
}
