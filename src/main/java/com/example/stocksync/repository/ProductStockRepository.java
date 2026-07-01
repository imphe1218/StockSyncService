package com.example.stocksync.repository;

import com.example.stocksync.domain.ProductStock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {
    Optional<ProductStock> findBySku(String sku);
}
