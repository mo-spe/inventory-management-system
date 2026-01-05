package com.example.inventorybackend.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("商超库存管理系统 API")
                        .version("3.2")
                        .description("版本3.2 - 推荐算法优化与异步处理")
                        .contact(new Contact().name("您的团队").email("a061027@qq.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org"))
                );
    }
}