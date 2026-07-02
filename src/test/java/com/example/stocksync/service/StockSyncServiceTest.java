package com.example.stocksync.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.stocksync.domain.ProductStock;
import com.example.stocksync.domain.StockEvent;
import com.example.stocksync.domain.StockItem;
import com.example.stocksync.repository.ProductStockRepository;
import com.example.stocksync.repository.StockEventRepository;
import com.example.stocksync.vendor.VendorStockClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockSyncServiceTest {

    private static final String SKU = "SKU-001";
    private static final String PRODUCT_NAME = "Gaming Mouse";
    private static final String VENDOR = "Vendor A";

    @Mock
    private VendorStockClient vendorStockClient;

    @Mock
    private ProductStockRepository productStockRepository;

    @Mock
    private StockEventRepository stockEventRepository;

    @Test
    void createsOutOfStockEventWhenQuantityMovesFromNonZeroToZero() {
        final StockSyncService stockSyncService = new StockSyncService(
                List.of(vendorStockClient),
                productStockRepository,
                stockEventRepository
        );

        when(vendorStockClient.fetchStock()).thenReturn(List.of(newStockItem(0)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.of(existingProductStock(10)));

        stockSyncService.synchronize();

        verify(stockEventRepository).save(any(StockEvent.class));
        verify(productStockRepository).save(any(ProductStock.class));
    }

    @Test
    void doesNotCreateOutOfStockEventWhenQuantityRemainsZero() {
        final StockSyncService stockSyncService = new StockSyncService(
                List.of(vendorStockClient),
                productStockRepository,
                stockEventRepository
        );

        when(vendorStockClient.fetchStock()).thenReturn(List.of(newStockItem(0)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.of(existingProductStock(0)));

        stockSyncService.synchronize();

        verify(stockEventRepository, never()).save(any(StockEvent.class));
        verify(productStockRepository).save(any(ProductStock.class));
    }

    @Test
    void doesNotCreateOutOfStockEventWhenQuantityMovesFromZeroToNonZero() {
        final StockSyncService stockSyncService = new StockSyncService(
                List.of(vendorStockClient),
                productStockRepository,
                stockEventRepository
        );

        when(vendorStockClient.fetchStock()).thenReturn(List.of(newStockItem(15)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.of(existingProductStock(0)));

        stockSyncService.synchronize();

        verify(stockEventRepository, never()).save(any(StockEvent.class));
        verify(productStockRepository).save(any(ProductStock.class));
    }

    private static StockItem newStockItem(final int quantity) {
        return new StockItem(SKU, PRODUCT_NAME, quantity, VENDOR);
    }

    private static ProductStock existingProductStock(final int quantity) {
        final ProductStock productStock =
                new ProductStock(SKU, PRODUCT_NAME, quantity, VENDOR);

        productStock.setLastUpdated(LocalDateTime.now());

        return productStock;
    }
}