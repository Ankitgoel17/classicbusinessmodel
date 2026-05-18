package com.project.cmb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @Column(name = "productCode", length = 15, nullable = false)
    private String productCode;

    @NotBlank(message = "Product name is required")
    @Size(max = 70, message = "Product name must not exceed 70 characters")
    @Column(name = "productName", length = 70, nullable = false)
    private String productName;

    @NotNull(message = "Product line is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productLine")
    private ProductLine productLine;

    @NotBlank(message = "Product scale is required")
    @Size(max = 10, message = "Product scale must not exceed 10 characters")
    @Column(name = "productScale", length = 10, nullable = false)
    private String productScale;

    @NotBlank(message = "Product vendor is required")
    @Size(max = 50, message = "Product vendor must not exceed 50 characters")
    @Column(name = "productVendor", length = 50, nullable = false)
    private String productVendor;

    @NotBlank(message = "Product description is required")
    @Column(name = "productDescription", columnDefinition = "TEXT", nullable = false)
    private String productDescription;

    @NotNull(message = "Quantity in stock is required")
    @Min(value = 0, message = "Quantity in stock cannot be negative")
    @Column(name = "quantityInStock", nullable = false)
    private Short quantityInStock;

    @NotNull(message = "Buy price is required")
    @DecimalMin(value = "0.01", message = "Buy price must be greater than 0")
    @Column(name = "buyPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal buyPrice;

    @NotNull(message = "MSRP is required")
    @DecimalMin(value = "0.01", message = "MSRP must be greater than 0")
    @Column(name = "MSRP", nullable = false, precision = 10, scale = 2)
    private BigDecimal msrp;
}