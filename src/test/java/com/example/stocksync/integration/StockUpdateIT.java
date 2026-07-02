package com.example.stocksync.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.stocksync.domain.ProductStock;
import com.example.stocksync.repository.ProductStockRepository;
import com.example.stocksync.repository.StockEventRepository;
import com.example.stocksync.service.StockSyncService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class StockUpdateIT {

    private static final Path VENDOR_B_CSV_PATH =
            Path.of("target/integration-test/vendor-b-stock-update.csv").toAbsolutePath();

    private static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(0);

    static {
        WIRE_MOCK_SERVER.start();
    }

    @Autowired
    private StockSyncService stockSyncService;

    @Autowired
    private ProductStockRepository productStockRepository;

    @Autowired
    private StockEventRepository stockEventRepository;

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("stock-sync.vendor-a.base-url", WIRE_MOCK_SERVER::baseUrl);
        registry.add("stock-sync.vendor-a.stock-path", () -> "/vendor-a/products");
        registry.add("stock-sync.vendor-b.csv-path", () -> VENDOR_B_CSV_PATH.toString());
        registry.add("stock-sync.schedule-ms", () -> "600000");
    }

    @BeforeEach
    void setUp() throws IOException {
        stockEventRepository.deleteAll();
        productStockRepository.deleteAll();
        WIRE_MOCK_SERVER.resetAll();

        Files.createDirectories(VENDOR_B_CSV_PATH.getParent());
    }

    @AfterAll
    static void tearDown() {
        WIRE_MOCK_SERVER.stop();
    }

    @Test
    void synchronizationUpdatesExistingProductsWhenVendorQuantitiesChange() throws IOException {
        stubVendorAProducts("""
                [
                  {"sku":"A-100","name":"Vendor A Keyboard","quantity":30}
                ]
                """);

        writeVendorBCsv("""
                sku,name,quantity
                B-100,Vendor B Monitor,15
                """);

        stockSyncService.synchronize();

        stubVendorAProducts("""
                [
                  {"sku":"A-100","name":"Vendor A Keyboard","quantity":25}
                ]
                """);

        writeVendorBCsv("""
                sku,name,quantity
                B-100,Vendor B Monitor,10
                """);

        stockSyncService.synchronize();

        final ProductStock vendorAProduct = productStockRepository.findBySku("A-100")
                .orElseThrow();

        final ProductStock vendorBProduct = productStockRepository.findBySku("B-100")
                .orElseThrow();

        assertThat(vendorAProduct.getQuantity()).isEqualTo(25);
        assertThat(vendorBProduct.getQuantity()).isEqualTo(10);

        assertThat(productStockRepository.findAll()).hasSize(2);
        assertThat(stockEventRepository.findAll()).isEmpty();
    }

    private static void stubVendorAProducts(final String responseBody) {
        WIRE_MOCK_SERVER.stubFor(WireMock.get(WireMock.urlEqualTo("/vendor-a/products"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
    }

    private static void writeVendorBCsv(final String csvContent) throws IOException {
        Files.writeString(VENDOR_B_CSV_PATH, csvContent);
    }
}