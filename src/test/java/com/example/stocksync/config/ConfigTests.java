package com.example.stocksync.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class ConfigTests {

    @Test
    void createsApplicationConfig() {
        ApplicationConfig config = new ApplicationConfig();

        assertThat(config).isNotNull();
    }

    @Test
    void createsWebClientBuilder() {
        WebClientConfig config = new WebClientConfig();

        WebClient.Builder builder = config.webClientBuilder();

        assertThat(builder).isNotNull();
    }

    @Test
    void createsStockSyncProperties() {
        StockSyncProperties properties = new StockSyncProperties(
                60000L,
                new StockSyncProperties.VendorA(
                        "http://localhost:8089",
                        "/vendor-a/products",
                        Duration.ofSeconds(5)
                ),
                new StockSyncProperties.VendorB(
                        "vendor-b/stock.csv",
                        Duration.ofSeconds(5)
                )
        );

        assertThat(properties.scheduleMs()).isEqualTo(60000L);
        assertThat(properties.vendorA().baseUrl()).isEqualTo("http://localhost:8089");
        assertThat(properties.vendorA().stockPath()).isEqualTo("/vendor-a/products");
        assertThat(properties.vendorA().timeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(properties.vendorB().csvPath()).isEqualTo("vendor-b/stock.csv");
        assertThat(properties.vendorB().timeout()).isEqualTo(Duration.ofSeconds(5));
    }
}