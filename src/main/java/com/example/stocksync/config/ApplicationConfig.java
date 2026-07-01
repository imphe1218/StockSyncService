package com.example.stocksync.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StockSyncProperties.class)
public class ApplicationConfig {
}
