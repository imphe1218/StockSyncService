package com.example.stocksync.vendor;

import com.example.stocksync.config.StockSyncProperties;
import com.example.stocksync.domain.StockItem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class VendorBCsvClient implements VendorStockClient {

    private static final String VENDOR = "VENDOR_B";
    private final StockSyncProperties properties;

    public VendorBCsvClient(final StockSyncProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<StockItem> fetchStock() {
        Path csvPath = Path.of(properties.vendorB().csvPath());
        if (!Files.exists(csvPath)) {
            return List.of();
        }

        try (CSVParser parser = CSVParser.parse(csvPath, StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.builder()
                        .setHeader("sku", "name", "quantity")
                        .setSkipHeaderRecord(true)
                        .setTrim(true)
                        .get())) {
            List<StockItem> items = new ArrayList<>();
            for (CSVRecord record : parser) {
                items.add(new StockItem(
                        record.get("sku"),
                        record.get("name"),
                        Integer.parseInt(record.get("quantity")),
                        VENDOR
                ));
            }
            return items;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read Vendor B CSV file", ex);
        }
    }
}
