package com.example.stocksync.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class StockSyncSchedulerTest {

    @Test
    void runScheduledSyncCallsStockSyncService() {
        StockSyncService stockSyncService = Mockito.mock(StockSyncService.class);
        StockSyncScheduler scheduler = new StockSyncScheduler(stockSyncService);

        scheduler.runScheduledSync();

        verify(stockSyncService).synchronize();
    }
}