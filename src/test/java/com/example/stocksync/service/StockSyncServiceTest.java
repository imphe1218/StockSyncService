package com.example.stocksync.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.stocksync.domain.StockItem;
import com.example.stocksync.repository.ProductStockRepository;
import com.example.stocksync.repository.StockEventRepository;
import com.example.stocksync.vendor.VendorStockClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(StockSyncServiceTest.TestConfig.class)
class StockSyncServiceTest {

    @Autowired
    private StockSyncService stockSyncService;

    @Autowired
    private ProductStockRepository productStockRepository;

    @Autowired
    private StockEventRepository stockEventRepository;

    @Test
    void synchronizeCreatesProductsAndOutOfStockEvent() {
        stockSyncService.synchronize();

        assertThat(productStockRepository.findAll()).hasSize(2);
        assertThat(stockEventRepository.findAll()).hasSize(1);
    }

    static class TestConfig {
        @org.springframework.context.annotation.Bean
        VendorStockClient vendorStockClient() {
            return () -> List.of(
                    new StockItem("SKU-1", "Test Item 1", 5, "TEST_VENDOR"),
                    new StockItem("SKU-2", "Test Item 2", 0, "TEST_VENDOR")
            );
        }

        @org.springframework.context.annotation.Bean
        StockSyncService stockSyncService(
                final List<VendorStockClient> clients,
                final ProductStockRepository productStockRepository,
                final StockEventRepository stockEventRepository
        ) {
            return new StockSyncService(clients, productStockRepository, stockEventRepository);
        }
    }
}
