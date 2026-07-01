package com.example.stocksync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@SuppressWarnings("PMD.UseUtilityClass")
public class StockSyncServiceApplication {

    public StockSyncServiceApplication() {

    }

    public static void main(final String[] args) {
        SpringApplication.run(StockSyncServiceApplication.class, args);
    }
}
