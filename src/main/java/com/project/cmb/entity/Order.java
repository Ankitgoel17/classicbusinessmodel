package com.project.cmb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @Column(name = "orderNumber")
    private Integer orderNumber;

    @NotNull(message = "Order date is required")
    @Column(name = "orderDate", nullable = false)
    private LocalDate orderDate;

    @NotNull(message = "Required date is required")
    @Column(name = "requiredDate", nullable = false)
    private LocalDate requiredDate;


    @Column(name = "shippedDate")
    private LocalDate shippedDate;

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "^(In Process|Shipped|Cancelled|Resolved|On Hold|Disputed)$",
            message = "Status must be one of: In Process, Shipped, Cancelled, Resolved, On Hold, Disputed"
    )
    @Column(name = "status", nullable = false)
    private String status;


    @Column(name = "comments")
    private String comments;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerNumber")
    private Customer customer;
}