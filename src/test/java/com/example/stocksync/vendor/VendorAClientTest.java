package com.example.stocksync.vendor;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.stocksync.config.StockSyncProperties;
import com.example.stocksync.domain.StockItem;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@WireMockTest(httpPort = 8089)
class VendorAClientTest {

    @Test
    void fetchStockReturnsMappedStockItems() {
        stubFor(get(urlEqualTo("/vendor-a/products"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "sku": "A-100",
                                    "name": "Vendor A Keyboard",
                                    "quantity": 30
                                  }
                                ]
                                """)));

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

        VendorAClient client = new VendorAClient(
                WebClient.builder(),
                properties,
                CircuitBreakerRegistry.ofDefaults(),
                RetryRegistry.ofDefaults()
        );

        List<StockItem> stockItems = client.fetchStock().block();

        assertThat(stockItems).hasSize(1);

        StockItem stockItem = stockItems.get(0);

        assertThat(stockItem.sku()).isEqualTo("A-100");
        assertThat(stockItem.name()).isEqualTo("Vendor A Keyboard");
        assertThat(stockItem.quantity()).isEqualTo(30);
        assertThat(stockItem.vendor()).isEqualTo("VENDOR_A");
    }
}