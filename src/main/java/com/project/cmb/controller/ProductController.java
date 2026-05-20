package com.project.cmb.controller;

import com.project.cmb.entity.Product;
import com.project.cmb.entity.ProductLine;
import com.project.cmb.exception.ResourceNotFoundException;
import com.project.cmb.projection.ProductListView;
import com.project.cmb.repo.ProductLineRepo;
import com.project.cmb.repo.ProductRepo;
import com.project.cmb.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/products")
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productCode}")
    public ResponseEntity<?> getProduct(@PathVariable String productCode) {

        return ResponseEntity.ok(productService.getProduct(productCode));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody Map<String, Object> dto) {

        return ResponseEntity.status(201).body(productService.addProduct(dto));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(productService.getStats());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductListView>> getLowStock() {

        return ResponseEntity.ok(productService.getLowStockProducts());
    }
}