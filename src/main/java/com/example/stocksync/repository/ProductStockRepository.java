package com.example.stocksync.repository;

import com.example.stocksync.domain.ProductStock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {
    Optional<ProductStock> findBySku(String sku);
}
