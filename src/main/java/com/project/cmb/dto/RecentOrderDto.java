package com.project.cmb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lightweight DTO used by the dashboard "recent orders" endpoint.
 * Carries a pre-computed orderTotal so the frontend doesn't need extra calls.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentOrderDto {

    private Integer orderNumber;
    private LocalDate orderDate;
    private String status;
    private CustomerInfo customer;
    private BigDecimal orderTotal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private Integer customerNumber;
        private String customerName;
    }
}
