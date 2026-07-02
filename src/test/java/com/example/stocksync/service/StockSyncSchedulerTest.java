package com.example.stocksync.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockSyncSchedulerTest {

    @Mock
    private StockSyncService stockSyncService;

    @Test
    void runScheduledSyncDelegatesToStockSyncService() {
        StockSyncScheduler scheduler = new StockSyncScheduler(stockSyncService);

        scheduler.runScheduledSync();

        verify(stockSyncService, times(1)).synchronize();
    }
}