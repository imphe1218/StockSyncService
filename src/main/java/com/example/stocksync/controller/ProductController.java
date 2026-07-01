package com.example.stocksync.controller;

import com.example.stocksync.dto.ProductStockResponse;
import com.example.stocksync.repository.ProductStockRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductStockRepository productStockRepository;

    public ProductController(final ProductStockRepository productStockRepository) {
        this.productStockRepository = productStockRepository;
    }

    @GetMapping
    public List<ProductStockResponse> listProducts() {
        return productStockRepository.findAll().stream()
                .map(ProductStockResponse::from)
                .toList();
    }
}
