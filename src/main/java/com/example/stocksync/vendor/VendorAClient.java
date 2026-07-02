package com.example.stocksync.vendor;

import com.example.stocksync.config.StockSyncProperties;
import com.example.stocksync.domain.StockItem;
import com.example.stocksync.dto.VendorAStockResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class VendorAClient implements VendorStockClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(VendorAClient.class);
    private static final String VENDOR = "VENDOR_A";
    private static final String CIRCUIT_BREAKER_NAME = "vendorA";
    private static final String RETRY_NAME = "vendorA";

    private final WebClient webClient;
    private final StockSyncProperties properties;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    public VendorAClient(
            final WebClient.Builder webClientBuilder,
            final StockSyncProperties properties,
            final CircuitBreakerRegistry circuitBreakerRegistry,
            final RetryRegistry retryRegistry
    ) {
        this.properties = properties;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.webClient = webClientBuilder.baseUrl(properties.vendorA().baseUrl()).build();
    }

    @Override
    public Mono<List<StockItem>> fetchStock() {
        return webClient.get()
                .uri(properties.vendorA().stockPath())
                .retrieve()
                .bodyToMono(VendorAStockResponse[].class)
                .timeout(properties.vendorA().timeout())
                .map(this::toStockItems)
                .defaultIfEmpty(List.of())
                .transformDeferred(RetryOperator.of(retryRegistry.retry(RETRY_NAME)))
                .transformDeferred(CircuitBreakerOperator.of(
                        circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME)))
                .doOnNext(items -> logSuccess(items.size()))
                .onErrorResume(this::handleFailure);
    }

    private List<StockItem> toStockItems(final VendorAStockResponse... response) {
        if (response == null) {
            return List.of();
        }

        return Arrays.stream(response)
                .map(item -> new StockItem(item.sku(), item.name(), item.quantity(), VENDOR))
                .toList();
    }

    private Mono<List<StockItem>> handleFailure(final Throwable ex) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(
                    "Vendor A stock fetch failed: {}. Skipping Vendor A for this sync.",
                    ex.getMessage()
            );
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Vendor A stock fetch failure details", ex);
        }

        return Mono.just(List.of());
    }

    private void logSuccess(final int itemCount) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Vendor A stock fetch completed with {} item(s)", itemCount);
        }
    }
}