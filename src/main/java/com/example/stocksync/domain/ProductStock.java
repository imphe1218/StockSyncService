package com.example.stocksync.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;


@Entity
@Table(name = "product_stock")
public class ProductStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String vendor;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    protected ProductStock() {
    }

    public ProductStock(final String sku, final String name, final int quantity, final String vendor) {
        this.sku = sku;
        this.name = name;
        this.quantity = quantity;
        this.vendor = vendor;
        this.lastUpdated = LocalDateTime.now();
    }

    public void updateFrom(final StockItem item) {
        this.name = item.name();
        this.quantity = item.quantity();
        this.vendor = item.vendor();
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public String getVendor() { return vendor; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}
