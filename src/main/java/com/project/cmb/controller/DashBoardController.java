package com.project.cmb.controller;

import com.project.cmb.dto.RecentOrderDto;
import com.project.cmb.service.DashBoardService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@AllArgsConstructor
public class DashBoardController {

    private final DashBoardService dashBoardService;

    // GET /api/v1/dashboard/stats
    @Cacheable("dashboard:stats")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalEmployees",     dashBoardService.getEmployeesCount());
        stats.put("totalCustomers",     dashBoardService.getCustomersCount());
        stats.put("totalOrders",        dashBoardService.getOrdersCount());
        stats.put("totalProducts",      dashBoardService.getProductsCount());
        stats.put("totalPayments",      dashBoardService.getPaymentsCount());
        stats.put("totalSalesAmount",   dashBoardService.getTotalSalesAmount());
        stats.put("pendingOrdersCount", dashBoardService.getPendingOrdersCount());
        return ResponseEntity.ok(stats);
    }

    // GET /api/v1/dashboard/recent-orders
    @Cacheable("dashboard:recent")
    @GetMapping("/recent-orders")
    public ResponseEntity<List<RecentOrderDto>> getRecentOrders() {
        return ResponseEntity.ok(dashBoardService.getRecentOrders());
    }

    // GET /api/v1/dashboard/orders-per-month
    @Cacheable("dashboard:orders-month")
    @GetMapping("/orders-per-month")
    public ResponseEntity<Map<String, Long>> getOrdersPerMonth() {
        return ResponseEntity.ok(dashBoardService.getOrdersPerMonth());
    }

    @Caching(evict = {
            @CacheEvict("dashboard:stats"),
            @CacheEvict("dashboard:recent"),
            @CacheEvict("dashboard:orders-month")
    })
    @DeleteMapping("/cache")
    public ResponseEntity<Void> evictDashboardCache() {
        return ResponseEntity.noContent().build();
    }
}
