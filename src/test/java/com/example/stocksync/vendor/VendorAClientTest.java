package com.example.stocksync.vendor;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.stocksync.config.StockSyncProperties;
import com.example.stocksync.domain.StockItem;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class VendorAClientTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void fetchStockReadsVendorAProducts() {
        stubFor(get(urlEqualTo("/vendor-a/products"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  { "sku": "A-1", "name": "Keyboard", "quantity": 3 }
                                ]
                                """)));

        StockSyncProperties.VendorA propertyVendorA =
                new StockSyncProperties.VendorA("http://localhost:" + wireMockServer.port(),
                        "/vendor-a/products");

        StockSyncProperties properties = new StockSyncProperties(300000L, propertyVendorA, null);

        VendorAClient client = new VendorAClient(WebClient.builder(), properties);
        List<StockItem> stockItems = client.fetchStock();

        assertThat(stockItems).containsExactly(new StockItem("A-1", "Keyboard", 3, "VENDOR_A"));
    }
}
