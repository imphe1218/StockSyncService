package com.example.stocksync.vendor;

import com.example.stocksync.config.StockSyncProperties;
import com.example.stocksync.domain.StockItem;
import com.example.stocksync.dto.VendorAStockResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class VendorAClient implements VendorStockClient {

    private static final String VENDOR = "VENDOR_A";
    private final WebClient webClient;
    private final StockSyncProperties properties;

    public VendorAClient(final WebClient.Builder webClientBuilder, final StockSyncProperties properties) {
        this.properties = properties;
        this.webClient = webClientBuilder.baseUrl(properties.vendorA().baseUrl()).build();
    }

    @Override
    public List<StockItem> fetchStock() {
        VendorAStockResponse[] response = webClient.get()
                .uri(properties.vendorA().stockPath())
                .retrieve()
                .bodyToMono(VendorAStockResponse[].class)
                .block();

        if (response == null) {
            return List.of();
        }

        return Arrays.stream(response)
                .map(item -> new StockItem(item.sku(), item.name(), item.quantity(), VENDOR))
                .toList();
    }
}
