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
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class IdempotentSynchronizationIT {

    private static final Path VENDOR_B_CSV_PATH =
            Path.of("target/integration-test/vendor-b-idempotent.csv").toAbsolutePath();

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
        productStockRepository.deleteAll();
        stockEventRepository.deleteAll();

        WIRE_MOCK_SERVER.resetAll();

        Files.createDirectories(VENDOR_B_CSV_PATH.getParent());

        Files.writeString(
                VENDOR_B_CSV_PATH,
                """
                sku,name,quantity
                B-100,Vendor B Monitor,15
                B-200,Vendor B Dock,7
                """
        );

        WIRE_MOCK_SERVER.stubFor(
                WireMock.get(WireMock.urlEqualTo("/vendor-a/products"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                                [
                                                  {
                                                    "sku":"A-100",
                                                    "name":"Vendor A Keyboard",
                                                    "quantity":30
                                                  },
                                                  {
                                                    "sku":"A-200",
                                                    "name":"Vendor A Mouse",
                                                    "quantity":5
                                                  }
                                                ]
                                                """)
                        )
        );
    }

    @AfterAll
    static void tearDown() {
        WIRE_MOCK_SERVER.stop();
    }

    @Test
    void repeatedSynchronizationWithIdenticalVendorDataIsIdempotent() {

        stockSyncService.synchronize();

        stockSyncService.synchronize();

        stockSyncService.synchronize();

        final List<ProductStock> products = productStockRepository.findAll();

        assertThat(products)
                .hasSize(4);

        assertThat(productStockRepository.findBySku("A-100"))
                .isPresent()
                .get()
                .extracting(ProductStock::getQuantity)
                .isEqualTo(30);

        assertThat(productStockRepository.findBySku("A-200"))
                .isPresent()
                .get()
                .extracting(ProductStock::getQuantity)
                .isEqualTo(5);

        assertThat(productStockRepository.findBySku("B-100"))
                .isPresent()
                .get()
                .extracting(ProductStock::getQuantity)
                .isEqualTo(15);

        assertThat(productStockRepository.findBySku("B-200"))
                .isPresent()
                .get()
                .extracting(ProductStock::getQuantity)
                .isEqualTo(7);

        assertThat(stockEventRepository.findAll())
                .isEmpty();
    }
}