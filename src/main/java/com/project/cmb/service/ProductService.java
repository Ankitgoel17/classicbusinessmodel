package com.project.cmb.service;

import com.project.cmb.entity.Product;
import com.project.cmb.entity.ProductLine;
import com.project.cmb.exception.ResourceNotFoundException;
import com.project.cmb.projection.ProductListView;
import com.project.cmb.repo.ProductLineRepo;
import com.project.cmb.repo.ProductRepo;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepo productRepo;
    private final ProductLineRepo productLineRepo;

    @Transactional(readOnly = true)
    public long getTotalProducts() {
        return productRepo.count();
    }

    @Transactional(readOnly = true)
    public long getTotalProductLines() {
        return productLineRepo.count();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getProductCountPerLine() {

        return productRepo.findAllProjectedBy()
                .stream()
                .collect(Collectors.groupingBy(
                        p -> p.getProductLine().getProductLine(),
                        Collectors.counting()
                ));
    }

    @Transactional(readOnly = true)
    public List<ProductListView> getLowStockProducts() {
        return productRepo.findByQuantityInStockLessThan((short) 50);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProduct(String productCode) {

        Product p = productRepo.findById(productCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", "productCode", productCode));

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("productCode", p.getProductCode());
        result.put("productName", p.getProductName());
        result.put("productScale", p.getProductScale());
        result.put("productVendor", p.getProductVendor());
        result.put("productDescription", p.getProductDescription());
        result.put("quantityInStock", p.getQuantityInStock());
        result.put("buyPrice", p.getBuyPrice());
        result.put("msrp", p.getMsrp());

        if (p.getProductLine() != null) {
            result.put("productLineName",
                    p.getProductLine().getProductLine());
        }

        return result;
    }

    public Map<String, Object> addProduct(Map<String, Object> dto) {

        Product p = new Product();

        p.setProductCode((String) dto.get("productCode"));
        p.setProductName((String) dto.get("productName"));
        p.setProductScale((String) dto.getOrDefault("productScale", "1:10"));
        p.setProductVendor((String) dto.getOrDefault("productVendor", ""));
        p.setProductDescription((String) dto.getOrDefault("productDescription", ""));

        if (dto.get("quantityInStock") != null) {
            p.setQuantityInStock(
                    Short.valueOf(dto.get("quantityInStock").toString()));
        }

        if (dto.get("buyPrice") != null) {
            p.setBuyPrice(
                    new BigDecimal(dto.get("buyPrice").toString()));
        }

        if (dto.get("msrp") != null) {
            p.setMsrp(
                    new BigDecimal(dto.get("msrp").toString()));
        }

        String productLineName = (String) dto.get("productLineName");

        if (productLineName == null || productLineName.isBlank()) {
            throw new IllegalArgumentException("productLineName is required");
        }

        ProductLine productLine = productLineRepo.findById(productLineName)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "ProductLine",
                                "productLine",
                                productLineName));

        p.setProductLine(productLine);

        if (p.getProductCode() == null || p.getProductCode().isBlank()) {
            throw new IllegalArgumentException("productCode is required");
        }

        if (productRepo.existsById(p.getProductCode())) {
            throw new IllegalArgumentException(
                    "Product code already exists");
        }

        Product saved = productRepo.save(p);

        return Map.of(
                "productCode", saved.getProductCode(),
                "message", "Product created"
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductListView> searchByName(
            String name,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        return productRepo.findByProductNameContainingIgnoreCase(
                name,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductListView> searchByVendor(
            String vendor,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        return productRepo.findByProductVendorContainingIgnoreCase(
                vendor,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductListView> searchByCode(
            String code,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        return productRepo.findByProductCodeContainingIgnoreCase(
                code,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductListView> filterByProductLine(
            String productLine,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        return productRepo.findByProductLine_ProductLine(
                productLine,
                pageable
        );
    }

    public Product updateBuyPrice(
            String productCode,
            BigDecimal buyPrice) {

        Product product = productRepo.findById(productCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product",
                                "productCode",
                                productCode));

        product.setBuyPrice(buyPrice);

        return productRepo.save(product);
    }

    public Product updateMsrp(
            String productCode,
            BigDecimal msrp) {

        Product product = productRepo.findById(productCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product",
                                "productCode",
                                productCode));

        product.setMsrp(msrp);

        return productRepo.save(product);
    }

    public Product updateQuantity(
            String productCode,
            Short quantity) {

        Product product = productRepo.findById(productCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product",
                                "productCode",
                                productCode));

        product.setQuantityInStock(quantity);

        return productRepo.save(product);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {

        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalProducts", getTotalProducts());

        stats.put("totalProductLines", getTotalProductLines());

        stats.put("productCountPerLine", getProductCountPerLine());

        return stats;
    }
}