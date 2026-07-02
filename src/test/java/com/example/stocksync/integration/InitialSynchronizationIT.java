package com.example.stocksync.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class InitialSynchronizationIT {

    private static final Path VENDOR_B_CSV_PATH =
            Path.of("target/integration-test/vendor-b-stock.csv").toAbsolutePath();

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

    @Autowired
    private MockMvc mockMvc;

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
    void initialSynchronizationPopulatesDatabaseAndApiReturnsProducts() throws Exception {
        stubVendorAProducts("""
                [
                  {"sku":"A-100","name":"Vendor A Keyboard","quantity":30},
                  {"sku":"A-200","name":"Vendor A Mouse","quantity":5}
                ]
                """);

        writeVendorBCsv("""
                sku,name,quantity
                B-100,Vendor B Monitor,15
                B-200,Vendor B Dock,7
                """);

        stockSyncService.synchronize();

        final List<ProductStock> products = productStockRepository.findAll();

        assertThat(products).hasSize(4);
        assertThat(productStockRepository.findBySku("A-100")).isPresent();
        assertThat(productStockRepository.findBySku("A-200")).isPresent();
        assertThat(productStockRepository.findBySku("B-100")).isPresent();
        assertThat(productStockRepository.findBySku("B-200")).isPresent();

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
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