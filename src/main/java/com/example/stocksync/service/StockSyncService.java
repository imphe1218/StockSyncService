package com.example.stocksync.service;

import com.example.stocksync.domain.ProductStock;
import com.example.stocksync.domain.StockEvent;
import com.example.stocksync.domain.StockItem;
import com.example.stocksync.repository.ProductStockRepository;
import com.example.stocksync.repository.StockEventRepository;
import com.example.stocksync.vendor.VendorStockClient;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockSyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockSyncService.class);
    private static final String OUT_OF_STOCK = "OUT_OF_STOCK";

    private final List<VendorStockClient> vendorStockClients;
    private final ProductStockRepository productStockRepository;
    private final StockEventRepository stockEventRepository;

    public StockSyncService(
            final List<VendorStockClient> vendorStockClients,
            final ProductStockRepository productStockRepository,
            final StockEventRepository stockEventRepository
    ) {
        this.vendorStockClients = List.copyOf(vendorStockClients);
        this.productStockRepository = productStockRepository;
        this.stockEventRepository = stockEventRepository;
    }

    @Transactional
    public void synchronize() {
        vendorStockClients.stream()
                .flatMap(client -> client.fetchStock().stream())
                .forEach(this::upsertStock);
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
