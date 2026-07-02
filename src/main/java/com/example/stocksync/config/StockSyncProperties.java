package com.example.stocksync.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stock-sync")
public record StockSyncProperties(
        long scheduleMs,
        VendorA vendorA,
        VendorB vendorB
) {

    public record VendorA(
            String baseUrl,
            String stockPath,
            Duration timeout
    ) {
    }

    public record VendorB(
            String csvPath,
            Duration timeout
    ) {
    }
}