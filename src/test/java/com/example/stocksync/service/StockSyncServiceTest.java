package com.example.stocksync.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;

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

    @Mock
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        lenient().doAnswer(invocation -> {
            final Consumer<TransactionStatus> callback = getTransactionCallback(invocation.getArgument(0));
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    void createsOutOfStockEventWhenQuantityMovesFromNonZeroToZero() {
        StockSyncService stockSyncService = newStockSyncService();

        when(vendorStockClient.fetchStock()).thenReturn(Mono.just(List.of(newStockItem(0))));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.of(existingProductStock(10)));

        stockSyncService.synchronize();

        verify(stockEventRepository).save(any(StockEvent.class));
        verify(productStockRepository).save(any(ProductStock.class));
    }

    @Test
    void doesNotCreateOutOfStockEventWhenQuantityRemainsZero() {
        StockSyncService stockSyncService = newStockSyncService();

        when(vendorStockClient.fetchStock()).thenReturn(Mono.just(List.of(newStockItem(0))));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.of(existingProductStock(0)));

        stockSyncService.synchronize();

        verify(stockEventRepository, never()).save(any(StockEvent.class));
        verify(productStockRepository).save(any(ProductStock.class));
    }

    @Test
    void doesNotCreateOutOfStockEventWhenQuantityMovesFromZeroToNonZero() {
        StockSyncService stockSyncService = newStockSyncService();

        when(vendorStockClient.fetchStock()).thenReturn(Mono.just(List.of(newStockItem(15))));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.of(existingProductStock(0)));

        stockSyncService.synchronize();

        verify(stockEventRepository, never()).save(any(StockEvent.class));
        verify(productStockRepository).save(any(ProductStock.class));
    }

    @Test
    void createsNewProductWhenSkuDoesNotExist() {
        StockSyncService stockSyncService = newStockSyncService();

        when(vendorStockClient.fetchStock()).thenReturn(Mono.just(List.of(newStockItem(8))));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.empty());

        stockSyncService.synchronize();

        ArgumentCaptor<ProductStock> productCaptor = forClass(ProductStock.class);
        verify(productStockRepository).save(productCaptor.capture());
        verify(stockEventRepository, never()).save(any(StockEvent.class));

        ProductStock savedProduct = productCaptor.getValue();
        assertThat(savedProduct.getSku()).isEqualTo(SKU);
        assertThat(savedProduct.getName()).isEqualTo(PRODUCT_NAME);
        assertThat(savedProduct.getQuantity()).isEqualTo(8);
        assertThat(savedProduct.getVendor()).isEqualTo(VENDOR);
    }

    @Test
    void createsOutOfStockEventWhenNewProductStartsAtZero() {
        StockSyncService stockSyncService = newStockSyncService();

        when(vendorStockClient.fetchStock()).thenReturn(Mono.just(List.of(newStockItem(0))));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.empty());

        stockSyncService.synchronize();

        verify(productStockRepository).save(any(ProductStock.class));
        verify(stockEventRepository).save(any(StockEvent.class));
    }

    @Test
    void doesNothingWhenVendorReturnsNoStock() {
        StockSyncService stockSyncService = newStockSyncService();

        when(vendorStockClient.fetchStock()).thenReturn(Mono.just(List.of()));

        stockSyncService.synchronize();

        verify(productStockRepository, never()).findBySku(any());
        verify(productStockRepository, never()).save(any(ProductStock.class));
        verify(stockEventRepository, never()).save(any(StockEvent.class));
    }

    @Test
    void persistsStockItemsOnBoundedElasticThread() {
        AtomicReference<String> transactionThreadName = new AtomicReference<>();

        doAnswer(invocation -> {
            transactionThreadName.set(Thread.currentThread().getName());
            final Consumer<TransactionStatus> callback = getTransactionCallback(invocation.getArgument(0));
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        StockSyncService stockSyncService = newStockSyncService();

        when(vendorStockClient.fetchStock()).thenReturn(Mono.just(List.of(newStockItem(10))));
        when(productStockRepository.findBySku(SKU)).thenReturn(Optional.empty());

        stockSyncService.synchronize();

        assertThat(transactionThreadName.get()).contains("boundedElastic");
    }

    private StockSyncService newStockSyncService() {
        return new StockSyncService(
                List.of(vendorStockClient),
                productStockRepository,
                stockEventRepository,
                transactionTemplate
        );
    }

    @SuppressWarnings("unchecked")
    private static Consumer<TransactionStatus> getTransactionCallback(final Object argument) {
        return (Consumer<TransactionStatus>) argument;
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