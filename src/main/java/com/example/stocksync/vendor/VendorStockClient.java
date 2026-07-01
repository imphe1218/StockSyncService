package com.example.stocksync.vendor;

import com.example.stocksync.domain.StockItem;
import java.util.List;


public interface VendorStockClient {
    List<StockItem> fetchStock();
}
