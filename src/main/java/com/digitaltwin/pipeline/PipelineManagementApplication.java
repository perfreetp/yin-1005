package com.digitaltwin.pipeline;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.digitaltwin.pipeline.mapper")
public class PipelineManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(PipelineManagementApplication.class, args);
    }
}
