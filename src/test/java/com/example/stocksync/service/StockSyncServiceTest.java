package com.example.stocksync.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.stocksync.domain.ProductStock;
import com.example.stocksync.domain.StockEvent;
import com.example.stocksync.domain.StockItem;
import com.example.stocksync.repository.ProductStockRepository;
import com.example.stocksync.repository.StockEventRepository;
import com.example.stocksync.vendor.VendorStockClient;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class StockSyncServiceTest {

    private static final String SKU = "SKU-001";
    private static final String PRODUCT_NAME = "Gaming Mouse";
    private static final String VENDOR = "VENDOR_A";

    @Mock
    private VendorStockClient vendorStockClient;

    @Mock
    private ProductStockRepository productStockRepository;

    @Mock
    private StockEventRepository stockEventRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private TransactionStatus transactionStatus;

    private StockSyncService stockSyncService;

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> {
            Consumer<TransactionStatus> action = invocation.getArgument(0);
            action.accept(transactionStatus);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        stockSyncService = new StockSyncService(
                List.of(vendorStockClient),
                productStockRepository,
                stockEventRepository,
                transactionTemplate
        );
    }

    @Test
    void createsNewProductStockWhenSkuDoesNotExist() {
        StockItem stockItem = new StockItem(SKU, PRODUCT_NAME, 10, VENDOR);

        when(vendorStockClient.fetchStock()).thenReturn(Mono.just(List.of(stockItem)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.empty());

        stockSyncService.synchronize();

        verify(productStockRepository).save(any(ProductStock.class));
        verify(stockEventRepository, never()).save(any(StockEvent.class));
    }

    @Test
    void createsOutOfStockEventWhenNewProductIsCreatedWithZeroQuantity() {
        StockItem stockItem = new StockItem(SKU, PRODUCT_NAME, 0, VENDOR);

        when(vendorStockClient.fetchStock()).thenReturn(Mono.just(List.of(stockItem)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.empty());

        stockSyncService.synchronize();

        verify(productStockRepository).save(any(ProductStock.class));
        verify(stockEventRepository).save(any(StockEvent.class));
    }

    @Test
    void createsOutOfStockEventWhenExistingProductMovesFromNonZeroToZero() {
        StockItem stockItem = new StockItem(SKU, PRODUCT_NAME, 0, VENDOR);
        ProductStock existingProductStock = new ProductStock(SKU, PRODUCT_NAME, 5, VENDOR);

        when(vendorStockClient.fetchStock()).thenReturn(Mono.just(List.of(stockItem)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.of(existingProductStock));

        stockSyncService.synchronize();

        verify(productStockRepository).save(existingProductStock);
        verify(stockEventRepository).save(any(StockEvent.class));
    }

    @Test
    void doesNotCreateOutOfStockEventWhenExistingProductRemainsInStock() {
        StockItem stockItem = new StockItem(SKU, PRODUCT_NAME, 7, VENDOR);
        ProductStock existingProductStock = new ProductStock(SKU, PRODUCT_NAME, 5, VENDOR);

        when(vendorStockClient.fetchStock()).thenReturn(Mono.just(List.of(stockItem)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.of(existingProductStock));

        stockSyncService.synchronize();

        verify(productStockRepository).save(existingProductStock);
        verify(stockEventRepository, never()).save(any(StockEvent.class));
    }

    @Test
    void doesNotCreateOutOfStockEventWhenExistingProductMovesFromZeroToNonZero() {
        StockItem stockItem = new StockItem(SKU, PRODUCT_NAME, 3, VENDOR);
        ProductStock existingProductStock = new ProductStock(SKU, PRODUCT_NAME, 0, VENDOR);

        when(vendorStockClient.fetchStock()).thenReturn(Mono.just(List.of(stockItem)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.of(existingProductStock));

        stockSyncService.synchronize();

        verify(productStockRepository).save(existingProductStock);
        verify(stockEventRepository, never()).save(any(StockEvent.class));
    }

    @Test
    void doesNotCreateOutOfStockEventWhenExistingProductRemainsZero() {
        StockItem stockItem = new StockItem(SKU, PRODUCT_NAME, 0, VENDOR);
        ProductStock existingProductStock = new ProductStock(SKU, PRODUCT_NAME, 0, VENDOR);

        when(vendorStockClient.fetchStock()).thenReturn(Mono.just(List.of(stockItem)));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.of(existingProductStock));

        stockSyncService.synchronize();

        verify(productStockRepository).save(existingProductStock);
        verify(stockEventRepository, never()).save(any(StockEvent.class));
    }
}