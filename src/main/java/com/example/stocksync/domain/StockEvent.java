package com.example.stocksync.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@SuppressWarnings("PMD.DataClass")
@Entity
@Table(name = "stock_event")
public class StockEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int previousQuantity;

    @Column(nullable = false)
    private int newQuantity;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected StockEvent() {
    }

    public StockEvent(final String sku, final int previousQuantity, final int newQuantity, final String eventType) {
        this.sku = sku;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.eventType = eventType;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public int getPreviousQuantity() { return previousQuantity; }
    public int getNewQuantity() { return newQuantity; }
    public String getEventType() { return eventType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
