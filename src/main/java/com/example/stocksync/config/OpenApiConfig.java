package com.example.stocksync.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI stockSyncOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Stock Sync Service API")
                        .version("v1")
                        .description("REST API documentation for Stock Sync Service."));
    }
}