package com.example.stocksync.service;

import static org.mockito.ArgumentCaptor.forClass;
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
import org.mockito.ArgumentCaptor;
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
        StockSyncService stockSyncService = newStockSyncService();

        when(vendorStockClient.fetchStock()).thenReturn(List.of(newStockItem(0)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.of(existingProductStock(10)));

        stockSyncService.synchronize();

        verify(stockEventRepository).save(any(StockEvent.class));
        verify(productStockRepository).save(any(ProductStock.class));
    }

    @Test
    void doesNotCreateOutOfStockEventWhenQuantityRemainsZero() {
        StockSyncService stockSyncService = newStockSyncService();

        when(vendorStockClient.fetchStock()).thenReturn(List.of(newStockItem(0)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.of(existingProductStock(0)));

        stockSyncService.synchronize();

        verify(stockEventRepository, never()).save(any(StockEvent.class));
        verify(productStockRepository).save(any(ProductStock.class));
    }

    @Test
    void doesNotCreateOutOfStockEventWhenQuantityMovesFromZeroToNonZero() {
        StockSyncService stockSyncService = newStockSyncService();

        when(vendorStockClient.fetchStock()).thenReturn(List.of(newStockItem(15)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.of(existingProductStock(0)));

        stockSyncService.synchronize();

        verify(stockEventRepository, never()).save(any(StockEvent.class));
        verify(productStockRepository).save(any(ProductStock.class));
    }

    @Test
    void createsNewProductStockWhenSkuDoesNotExist() {
        StockSyncService stockSyncService = newStockSyncService();

        when(vendorStockClient.fetchStock()).thenReturn(List.of(newStockItem(5)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.empty());

        stockSyncService.synchronize();

        ArgumentCaptor<ProductStock> captor = forClass(ProductStock.class);
        verify(productStockRepository).save(captor.capture());
        verify(stockEventRepository, never()).save(any(StockEvent.class));

        ProductStock saved = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(saved.getSku()).isEqualTo(SKU);
        org.assertj.core.api.Assertions.assertThat(saved.getName()).isEqualTo(PRODUCT_NAME);
        org.assertj.core.api.Assertions.assertThat(saved.getQuantity()).isEqualTo(5);
        org.assertj.core.api.Assertions.assertThat(saved.getVendor()).isEqualTo(VENDOR);
    }

    @Test
    void createsOutOfStockEventWhenNewProductStartsAtZero() {
        StockSyncService stockSyncService = newStockSyncService();

        when(vendorStockClient.fetchStock()).thenReturn(List.of(newStockItem(0)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.empty());

        stockSyncService.synchronize();

        verify(productStockRepository).save(any(ProductStock.class));
        verify(stockEventRepository).save(any(StockEvent.class));
    }

    @Test
    void doesNothingWhenVendorReturnsNoStock() {
        StockSyncService stockSyncService = newStockSyncService();

        when(vendorStockClient.fetchStock()).thenReturn(List.of());

        stockSyncService.synchronize();

        verify(productStockRepository, never()).findBySku(any());
        verify(productStockRepository, never()).save(any(ProductStock.class));
        verify(stockEventRepository, never()).save(any(StockEvent.class));
    }

    private StockSyncService newStockSyncService() {
        return new StockSyncService(
                List.of(vendorStockClient),
                productStockRepository,
                stockEventRepository
        );
    }

    private static StockItem newStockItem(final int quantity) {
        return new StockItem(SKU, PRODUCT_NAME, quantity, VENDOR);
    }

    private static ProductStock existingProductStock(final int quantity) {
        ProductStock productStock = new ProductStock(SKU, PRODUCT_NAME, quantity, VENDOR);
        productStock.setLastUpdated(LocalDateTime.now());
        return productStock;
    }
}