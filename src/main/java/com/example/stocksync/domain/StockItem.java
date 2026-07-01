package com.example.stocksync.domain;

public record StockItem(String sku, String name, int quantity, String vendor) {
}
