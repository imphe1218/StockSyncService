package com.example.stocksync.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StockSyncScheduler {

    private final StockSyncService stockSyncService;

    public StockSyncScheduler(final StockSyncService stockSyncService) {
        this.stockSyncService = stockSyncService;
    }

    @Scheduled(fixedDelayString = "${stock-sync.schedule-ms}")
    public void runScheduledSync() {
        stockSyncService.synchronize();
    }
}
