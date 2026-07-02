package com.example.stocksync.service;

import com.example.stocksync.domain.ProductStock;
import com.example.stocksync.domain.StockEvent;
import com.example.stocksync.domain.StockItem;
import com.example.stocksync.repository.ProductStockRepository;
import com.example.stocksync.repository.StockEventRepository;
import com.example.stocksync.vendor.VendorStockClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Service
public class StockSyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockSyncService.class);
    private static final String OUT_OF_STOCK = "OUT_OF_STOCK";

    private final List<VendorStockClient> vendorStockClients;
    private final ProductStockRepository productStockRepository;
    private final StockEventRepository stockEventRepository;
    private final TransactionTemplate transactionTemplate;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "TransactionTemplate is a Spring-managed infrastructure bean intended to be shared."
    )
    public StockSyncService(
            final List<VendorStockClient> vendorStockClients,
            final ProductStockRepository productStockRepository,
            final StockEventRepository stockEventRepository,
            final TransactionTemplate transactionTemplate
    ) {
        this.vendorStockClients = List.copyOf(vendorStockClients);
        this.productStockRepository = productStockRepository;
        this.stockEventRepository = stockEventRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public void synchronize() {
        synchronizeReactive().block();
    }

    Mono<Void> synchronizeReactive() {
        return Flux.fromIterable(vendorStockClients)
                .flatMap(VendorStockClient::fetchStock)
                .flatMapIterable(items -> items)
                .collectList()
                .flatMap(this::persistStockItems)
                .then();
    }

    private Mono<Void> persistStockItems(final List<StockItem> items) {
        return Mono.fromRunnable(() -> transactionTemplate.executeWithoutResult(
                        status -> syncToDatabase(items)))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private void syncToDatabase(final List<StockItem> items) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Persisting {} stock item(s)", items.size());
        }

        items.forEach(this::upsertStock);
    }

    private void upsertStock(final StockItem item) {
        productStockRepository.findBySku(item.sku())
                .ifPresentOrElse(existing -> updateExistingStock(existing, item), () -> createNewStock(item));
    }

    private void updateExistingStock(final ProductStock existing, final StockItem item) {
        int previousQuantity = existing.getQuantity();

        existing.updateFrom(item);
        productStockRepository.save(existing);

        if (previousQuantity > 0 && item.quantity() == 0) {
            stockEventRepository.save(new StockEvent(item.sku(), previousQuantity, item.quantity(), OUT_OF_STOCK));
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Product {} transitioned out of stock", item.sku());
            }
        }
    }

    private void createNewStock(final StockItem item) {
        productStockRepository.save(new ProductStock(item.sku(), item.name(), item.quantity(), item.vendor()));

        if (item.quantity() == 0) {
            stockEventRepository.save(new StockEvent(item.sku(), 0, 0, OUT_OF_STOCK));
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Product {} created as out of stock", item.sku());
            }
        }
    }
}