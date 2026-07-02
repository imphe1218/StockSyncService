package com.example.stocksync.controller;

import com.example.stocksync.domain.ProductStock;
import com.example.stocksync.repository.ProductStockRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.base-path:/api/v1}/products")
public class ProductController {

    private final ProductStockRepository productStockRepository;

    public ProductController(final ProductStockRepository productStockRepository) {
        this.productStockRepository = productStockRepository;
    }

    @GetMapping
    public List<ProductStock> getProducts() {
        return productStockRepository.findAll();
    }
}