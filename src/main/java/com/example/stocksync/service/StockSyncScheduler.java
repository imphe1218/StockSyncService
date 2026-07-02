package com.example.stocksync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StockSyncScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockSyncScheduler.class);

    private final StockSyncService stockSyncService;

    public StockSyncScheduler(final StockSyncService stockSyncService) {
        this.stockSyncService = stockSyncService;
    }

    @Scheduled(fixedDelayString = "${stock-sync.schedule-ms}")
    public void runScheduledSync() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Scheduled stock synchronization started");
        }

        stockSyncService.synchronize();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Scheduled stock synchronization completed");
        }
    }
}
