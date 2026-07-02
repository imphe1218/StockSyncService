package com.example.stocksync.vendor;

import com.example.stocksync.domain.StockItem;
import java.util.List;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface VendorStockClient {

    Mono<List<StockItem>> fetchStock();
}