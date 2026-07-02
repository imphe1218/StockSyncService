package com.example.stocksync.vendor;

import com.example.stocksync.config.StockSyncProperties;
import com.example.stocksync.domain.StockItem;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.RetryRegistry;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class VendorBCsvClient implements VendorStockClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(VendorBCsvClient.class);
    private static final String VENDOR = "VENDOR_B";
    private static final String CIRCUIT_BREAKER_NAME = "vendorB";
    private static final String RETRY_NAME = "vendorB";

    private final StockSyncProperties properties;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    public VendorBCsvClient(
            final StockSyncProperties properties,
            final CircuitBreakerRegistry circuitBreakerRegistry,
            final RetryRegistry retryRegistry
    ) {
        this.properties = properties;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
    }

    @Override
    public Mono<List<StockItem>> fetchStock() {
        return Mono.fromCallable(this::readCsvFile)
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(properties.vendorB().timeout())
                .transformDeferred(RetryOperator.of(retryRegistry.retry(RETRY_NAME)))
                .transformDeferred(CircuitBreakerOperator.of(
                        circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME)))
                .doOnNext(items -> logSuccess(items.size()))
                .onErrorResume(this::handleFailure);
    }

    private List<StockItem> readCsvFile() throws IOException {
        Path csvPath = Path.of(properties.vendorB().csvPath());

        if (!Files.exists(csvPath)) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Vendor B CSV file does not exist: {}", csvPath);
            }
            return List.of();
        }

        try (CSVParser parser = CSVParser.parse(
                csvPath,
                StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.builder()
                        .setHeader("sku", "name", "quantity")
                        .setSkipHeaderRecord(true)
                        .setTrim(true)
                        .get())) {

            List<StockItem> items = new ArrayList<>();

            for (CSVRecord record : parser) {
                parseRecord(record).ifPresent(items::add);
            }

            return items;
        }
    }

    private java.util.Optional<StockItem> parseRecord(final CSVRecord record) {
        String sku = record.get("sku");
        String name = record.get("name");
        String quantity = record.get("quantity");

        if (sku.isBlank() || name.isBlank() || quantity.isBlank()) {
            logSkippedRecord(record, "missing required field");
            return java.util.Optional.empty();
        }

        try {
            int parsedQuantity = Integer.parseInt(quantity);

            if (parsedQuantity < 0) {
                logSkippedRecord(record, "negative quantity");
                return java.util.Optional.empty();
            }

            return java.util.Optional.of(new StockItem(sku, name, parsedQuantity, VENDOR));
        } catch (NumberFormatException ex) {
            logSkippedRecord(record, "invalid quantity");
            return java.util.Optional.empty();
        }
    }

    private Mono<List<StockItem>> handleFailure(final Throwable ex) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Vendor B CSV stock fetch failed. Skipping Vendor B for this sync.", ex);
        }
        return Mono.just(List.of());
    }

    private void logSuccess(final int itemCount) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Vendor B CSV stock fetch completed with {} item(s)", itemCount);
        }
    }

    private void logSkippedRecord(final CSVRecord record, final String reason) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Skipping Vendor B CSV record {} because {}", record.getRecordNumber(), reason);
        }
    }
}