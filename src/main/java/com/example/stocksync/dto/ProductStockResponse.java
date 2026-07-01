package com.example.stocksync.dto;

import com.example.stocksync.domain.ProductStock;
import java.time.LocalDateTime;

public record ProductStockResponse(
        String sku,
        String name,
        int quantity,
        String vendor,
        LocalDateTime lastUpdated
) {
    public static ProductStockResponse from(final ProductStock productStock) {
        return new ProductStockResponse(
                productStock.getSku(),
                productStock.getName(),
                productStock.getQuantity(),
                productStock.getVendor(),
                productStock.getLastUpdated()
        );
    }
}
