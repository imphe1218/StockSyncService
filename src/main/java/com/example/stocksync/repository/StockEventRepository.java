package com.example.stocksync.repository;

import com.example.stocksync.domain.StockEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockEventRepository extends JpaRepository<StockEvent, Long> {
}
