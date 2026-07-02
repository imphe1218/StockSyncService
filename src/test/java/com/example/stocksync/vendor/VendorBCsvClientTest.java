package com.example.stocksync.vendor;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.stocksync.config.StockSyncProperties;
import com.example.stocksync.domain.StockItem;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VendorBCsvClientTest {

    @TempDir
    private Path tempDir;

    @Test
    void fetchStockReturnsMappedStockItemsFromCsv() throws Exception {
        Path csvPath = tempDir.resolve("stock.csv");

        Files.writeString(csvPath, """
                sku,name,quantity
                B-100,Vendor B Monitor,15
                """);

        VendorBCsvClient client = newClient(csvPath);

        List<StockItem> stockItems = client.fetchStock().block();

        assertThat(stockItems).hasSize(1);

        StockItem stockItem = stockItems.get(0);

        assertThat(stockItem.sku()).isEqualTo("B-100");
        assertThat(stockItem.name()).isEqualTo("Vendor B Monitor");
        assertThat(stockItem.quantity()).isEqualTo(15);
        assertThat(stockItem.vendor()).isEqualTo("VENDOR_B");
    }

    @Test
    void fetchStockReturnsEmptyListWhenCsvFileDoesNotExist() {
        Path csvPath = tempDir.resolve("missing.csv");

        VendorBCsvClient client = newClient(csvPath);

        List<StockItem> stockItems = client.fetchStock().block();

        assertThat(stockItems).isEmpty();
    }

    @Test
    void fetchStockSkipsInvalidCsvRows() throws Exception {
        Path csvPath = tempDir.resolve("stock.csv");

        Files.writeString(csvPath, """
                sku,name,quantity
                B-100,Vendor B Monitor,15
                B-200,Vendor B Dock,invalid
                B-300,Vendor B Stand,-1
                ,Missing SKU,5
                """);

        VendorBCsvClient client = newClient(csvPath);

        List<StockItem> stockItems = client.fetchStock().block();

        assertThat(stockItems).hasSize(1);
        assertThat(stockItems.get(0).sku()).isEqualTo("B-100");
    }

    private VendorBCsvClient newClient(final Path csvPath) {
        return new VendorBCsvClient(
                properties(csvPath),
                CircuitBreakerRegistry.ofDefaults(),
                RetryRegistry.ofDefaults()
        );
    }

    private StockSyncProperties properties(final Path csvPath) {
        return new StockSyncProperties(
                60000L,
                new StockSyncProperties.VendorA(
                        "http://localhost:8089",
                        "/vendor-a/products",
                        Duration.ofSeconds(3)
                ),
                new StockSyncProperties.VendorB(
                        csvPath.toString(),
                        Duration.ofSeconds(2)
                )
        );
    }
}