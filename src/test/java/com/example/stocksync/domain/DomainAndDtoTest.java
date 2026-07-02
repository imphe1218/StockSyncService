package com.example.stocksync.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.stocksync.dto.ProductStockResponse;
import com.example.stocksync.dto.VendorAStockResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DomainAndDtoTest {

    @Test
    void productStockUpdatesFromStockItem() {
        ProductStock productStock = new ProductStock("SKU-1", "Old Name", 3, "VENDOR_A");
        LocalDateTime previousLastUpdated = productStock.getLastUpdated();

        productStock.updateFrom(new StockItem("SKU-1", "New Name", 9, "VENDOR_B"));

        assertThat(productStock.getSku()).isEqualTo("SKU-1");
        assertThat(productStock.getName()).isEqualTo("New Name");
        assertThat(productStock.getQuantity()).isEqualTo(9);
        assertThat(productStock.getVendor()).isEqualTo("VENDOR_B");
        assertThat(productStock.getLastUpdated()).isAfterOrEqualTo(previousLastUpdated);
    }

    @Test
    void productStockSettersAndGettersWork() {
        ProductStock productStock = new ProductStock("SKU-1", "Item", 1, "VENDOR_A");
        LocalDateTime now = LocalDateTime.now();

        productStock.setSku("SKU-2");
        productStock.setQuantity(5);
        productStock.setVendor("VENDOR_B");
        productStock.setLastUpdated(now);

        assertThat(productStock.getId()).isNull();
        assertThat(productStock.getSku()).isEqualTo("SKU-2");
        assertThat(productStock.getQuantity()).isEqualTo(5);
        assertThat(productStock.getVendor()).isEqualTo("VENDOR_B");
        assertThat(productStock.getLastUpdated()).isEqualTo(now);
    }

    @Test
    void stockEventGettersWork() {
        StockEvent event = new StockEvent("SKU-1", 10, 0, "OUT_OF_STOCK");

        assertThat(event.getId()).isNull();
        assertThat(event.getSku()).isEqualTo("SKU-1");
        assertThat(event.getPreviousQuantity()).isEqualTo(10);
        assertThat(event.getNewQuantity()).isZero();
        assertThat(event.getEventType()).isEqualTo("OUT_OF_STOCK");
        assertThat(event.getCreatedAt()).isNotNull();
    }

    @Test
    void productStockResponseConvertsFromEntity() {
        ProductStock productStock = new ProductStock("SKU-1", "Keyboard", 7, "VENDOR_A");

        ProductStockResponse response = ProductStockResponse.from(productStock);

        assertThat(response.sku()).isEqualTo("SKU-1");
        assertThat(response.name()).isEqualTo("Keyboard");
        assertThat(response.quantity()).isEqualTo(7);
        assertThat(response.vendor()).isEqualTo("VENDOR_A");
        assertThat(response.lastUpdated()).isEqualTo(productStock.getLastUpdated());
    }

    @Test
    void vendorAStockResponseRecordWorks() {
        VendorAStockResponse response = new VendorAStockResponse("A-100", "Keyboard", 30);

        assertThat(response.sku()).isEqualTo("A-100");
        assertThat(response.name()).isEqualTo("Keyboard");
        assertThat(response.quantity()).isEqualTo(30);
    }
}