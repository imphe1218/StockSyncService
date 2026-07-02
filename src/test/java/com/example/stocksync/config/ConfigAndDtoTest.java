package com.example.stocksync.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.stocksync.domain.ProductStock;
import com.example.stocksync.dto.ProductStockResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ConfigAndDtoTest {

    @Test
    void stockSyncPropertiesStoresConfiguredValues() {
        StockSyncProperties properties = new StockSyncProperties(
                60000L,
                new StockSyncProperties.VendorA(
                        "http://localhost:8089",
                        "/vendor-a/products",
                        Duration.ofSeconds(3)
                ),
                new StockSyncProperties.VendorB(
                        "/tmp/vendor-b/stock.csv",
                        Duration.ofSeconds(2)
                )
        );

        assertThat(properties.scheduleMs()).isEqualTo(60000L);
        assertThat(properties.vendorA().baseUrl()).isEqualTo("http://localhost:8089");
        assertThat(properties.vendorA().stockPath()).isEqualTo("/vendor-a/products");
        assertThat(properties.vendorA().timeout()).isEqualTo(Duration.ofSeconds(3));
        assertThat(properties.vendorB().csvPath()).isEqualTo("/tmp/vendor-b/stock.csv");
        assertThat(properties.vendorB().timeout()).isEqualTo(Duration.ofSeconds(2));
    }

    @Test
    void productStockResponseMapsFromProductStock() {
        ProductStock productStock = new ProductStock(
                "SKU-001",
                "Gaming Mouse",
                10,
                "VENDOR_A"
        );

        ProductStockResponse response = ProductStockResponse.from(productStock);

        assertThat(response.sku()).isEqualTo("SKU-001");
        assertThat(response.name()).isEqualTo("Gaming Mouse");
        assertThat(response.quantity()).isEqualTo(10);
        assertThat(response.vendor()).isEqualTo("VENDOR_A");
        assertThat(response.lastUpdated()).isEqualTo(productStock.getLastUpdated());
    }
}