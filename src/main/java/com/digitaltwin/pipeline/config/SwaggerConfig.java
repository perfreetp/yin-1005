package com.digitaltwin.pipeline.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("数字孪生城市地下管线管理系统 API")
                        .description("面向管线一张图、施工审批端和巡检 App 提供统一接口")
                        .version("1.0.0")
                        .contact(new Contact().name("DigitalTwin Team").email("support@digitaltwin.com"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
