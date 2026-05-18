package com.project.cmb.controller;

import com.project.cmb.entity.Product;
import com.project.cmb.exception.ResourceNotFoundException;
import com.project.cmb.projection.ProductListView;
import com.project.cmb.repo.ProductRepo;
import com.project.cmb.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@AllArgsConstructor
public class ProductController {

    private final ProductRepo productRepo;
    private final ProductService productService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productService.getTotalProducts());
        stats.put("totalProductLines", productService.getTotalProductLines());
        stats.put("productCountPerLine", productService.getProductCountPerLine());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductListView>> getLowStock() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductListView>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                productRepo.findByProductNameContainingIgnoreCase(name, pageable));
    }

    @GetMapping("/search/vendor")
    public ResponseEntity<Page<ProductListView>> searchByVendor(
            @RequestParam String vendor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                productRepo.findByProductVendorContainingIgnoreCase(vendor, pageable));
    }

    @GetMapping("/search/code")
    public ResponseEntity<Page<ProductListView>> searchByCode(
            @RequestParam String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                productRepo.findByProductCodeContainingIgnoreCase(code, pageable));
    }

    @GetMapping("/filter/line")
    public ResponseEntity<Page<ProductListView>> filterByProductLine(
            @RequestParam String productLine,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                productRepo.findByProductLine_ProductLine(productLine, pageable));
    }

    @PutMapping("/{productCode}/price/{buyPrice}")
    public ResponseEntity<Product> updateBuyPrice(
            @PathVariable String productCode,
            @PathVariable BigDecimal buyPrice) {
        Product product = productRepo.findById(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productCode", productCode));
        product.setBuyPrice(buyPrice);
        return ResponseEntity.ok(productRepo.save(product));
    }

    @PutMapping("/{productCode}/msrp/{msrp}")
    public ResponseEntity<Product> updateMsrp(
            @PathVariable String productCode,
            @PathVariable BigDecimal msrp) {
        Product product = productRepo.findById(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productCode", productCode));
        product.setMsrp(msrp);
        return ResponseEntity.ok(productRepo.save(product));
    }

    @PutMapping("/{productCode}/quantity/{quantity}")
    public ResponseEntity<Product> updateQuantity(
            @PathVariable String productCode,
            @PathVariable Short quantity) {
        Product product = productRepo.findById(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productCode", productCode));
        product.setQuantityInStock(quantity);
        return ResponseEntity.ok(productRepo.save(product));
    }
}