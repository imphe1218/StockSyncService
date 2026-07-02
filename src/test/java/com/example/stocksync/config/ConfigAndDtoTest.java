package com.example.stocksync.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.stocksync.domain.ProductStock;
import com.example.stocksync.dto.ProductStockResponse;
import com.example.stocksync.dto.VendorAStockResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class ConfigAndDtoTest {

    @Test
    void createsWebClientBuilderBean() {
        WebClientConfig config = new WebClientConfig();

        WebClient.Builder builder = config.webClientBuilder();

        assertThat(builder).isNotNull();
    }

    @Test
    void createsApplicationConfig() {
        assertThat(new ApplicationConfig()).isNotNull();
    }

    @Test
    void createsStockSyncProperties() {
        StockSyncProperties properties = new StockSyncProperties(
                60000L,
                new StockSyncProperties.VendorA("http://localhost:8089", "/vendor-a/products"),
                new StockSyncProperties.VendorB("vendor-b/stock.csv")
        );

        assertThat(properties.scheduleMs()).isEqualTo(60000L);
        assertThat(properties.vendorA().baseUrl()).isEqualTo("http://localhost:8089");
        assertThat(properties.vendorA().stockPath()).isEqualTo("/vendor-a/products");
        assertThat(properties.vendorB().csvPath()).isEqualTo("vendor-b/stock.csv");
    }

    @Test
    void convertsProductStockToResponse() {
        ProductStock productStock = new ProductStock("SKU-1", "Keyboard", 3, "VENDOR_A");

        ProductStockResponse response = ProductStockResponse.from(productStock);

        assertThat(response.sku()).isEqualTo("SKU-1");
        assertThat(response.name()).isEqualTo("Keyboard");
        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.vendor()).isEqualTo("VENDOR_A");
        assertThat(response.lastUpdated()).isNotNull();
    }

    @Test
    void createsVendorAStockResponse() {
        VendorAStockResponse response = new VendorAStockResponse("A-1", "Mouse", 9);

        assertThat(response.sku()).isEqualTo("A-1");
        assertThat(response.name()).isEqualTo("Mouse");
        assertThat(response.quantity()).isEqualTo(9);
    }
}