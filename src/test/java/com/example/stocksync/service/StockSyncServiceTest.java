package com.example.stocksync.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.stocksync.domain.ProductStock;
import com.example.stocksync.domain.StockItem;
import com.example.stocksync.repository.ProductStockRepository;
import com.example.stocksync.repository.StockEventRepository;
import com.example.stocksync.vendor.VendorStockClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
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

    @Autowired
    private MutableVendorStockClient vendorStockClient;

    @Test
    void synchronizeCreatesProductsAndOutOfStockEvent() {
        vendorStockClient.setStockItems(List.of(
                new StockItem("SKU-1", "Test Item 1", 5, "TEST_VENDOR"),
                new StockItem("SKU-2", "Test Item 2", 0, "TEST_VENDOR")
        ));

        stockSyncService.synchronize();

        assertThat(productStockRepository.findAll()).hasSize(2);
        assertThat(stockEventRepository.findAll()).hasSize(1);
    }

    @Test
    void synchronizeUpdatesExistingProduct() {
        productStockRepository.save(new ProductStock("SKU-1", "Old Name", 5, "OLD_VENDOR"));
        vendorStockClient.setStockItems(List.of(
                new StockItem("SKU-1", "New Name", 8, "NEW_VENDOR")
        ));

        stockSyncService.synchronize();

        ProductStock productStock = productStockRepository.findBySku("SKU-1").orElseThrow();

        assertThat(productStock.getName()).isEqualTo("New Name");
        assertThat(productStock.getQuantity()).isEqualTo(8);
        assertThat(productStock.getVendor()).isEqualTo("NEW_VENDOR");
        assertThat(stockEventRepository.findAll()).isEmpty();
    }

    @Test
    void synchronizeCreatesOutOfStockEventWhenExistingProductTransitionsToZero() {
        productStockRepository.save(new ProductStock("SKU-1", "Item", 5, "TEST_VENDOR"));
        vendorStockClient.setStockItems(List.of(
                new StockItem("SKU-1", "Item", 0, "TEST_VENDOR")
        ));

        stockSyncService.synchronize();

        assertThat(productStockRepository.findBySku("SKU-1").orElseThrow().getQuantity()).isZero();
        assertThat(stockEventRepository.findAll()).hasSize(1);
    }

    @Test
    void synchronizeDoesNotCreateEventWhenExistingProductRemainsOutOfStock() {
        productStockRepository.save(new ProductStock("SKU-1", "Item", 0, "TEST_VENDOR"));
        vendorStockClient.setStockItems(List.of(
                new StockItem("SKU-1", "Item", 0, "TEST_VENDOR")
        ));

        stockSyncService.synchronize();

        assertThat(stockEventRepository.findAll()).isEmpty();
    }

    static class TestConfig {

        @Bean
        MutableVendorStockClient vendorStockClient() {
            return new MutableVendorStockClient();
        }

        @Bean
        StockSyncService stockSyncService(
                final List<VendorStockClient> clients,
                final ProductStockRepository productStockRepository,
                final StockEventRepository stockEventRepository
        ) {
            return new StockSyncService(clients, productStockRepository, stockEventRepository);
        }
    }

    static class MutableVendorStockClient implements VendorStockClient {

        private List<StockItem> stockItems = List.of();

        void setStockItems(final List<StockItem> stockItems) {
            this.stockItems = List.copyOf(stockItems);
        }

        @Override
        public List<StockItem> fetchStock() {
            return stockItems;
        }
    }
}