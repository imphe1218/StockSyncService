package com.example.stocksync.vendor;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.stocksync.config.StockSyncProperties;
import com.example.stocksync.domain.StockItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VendorBCsvClientTest {

    @TempDir
    private Path tempDir;

    @Test
    void fetchStockReadsCsvFile() throws Exception {
        Path csv = tempDir.resolve("stock.csv");
        Files.writeString(csv, "sku,name,quantity\nB-1,Monitor,7\n");

        StockSyncProperties.VendorB propertyVendorB = new StockSyncProperties.VendorB(csv.toString());

        StockSyncProperties properties = new StockSyncProperties(300000L, null , propertyVendorB);

        VendorBCsvClient client = new VendorBCsvClient(properties);
        List<StockItem> stockItems = client.fetchStock();

        assertThat(stockItems).containsExactly(new StockItem("B-1", "Monitor", 7, "VENDOR_B"));
    }
}
