package com.clipflow.common.config;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clipFlowOpenAPI() {

        Info info = new Info()
                .title("ClipFlow API")
                .description(
                        "短视频社交后端接口文档"
                )
                .version("1.0.0");

        SecurityScheme jwtScheme =
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT");

        return new OpenAPI()
                .info(info)
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "BearerAuth",
                                        jwtScheme
                                )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("BearerAuth")
                );
    }
}