package com.project.cmb.controller;

import com.project.cmb.projection.RecentOrderView;
import com.project.cmb.service.DashBoardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@AllArgsConstructor
public class DashBoardController {

    private final DashBoardService dashBoardService;

    // GET /api/v1/dashboard/stats
    // Returns all counts in one call — reduces frontend round trips
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", dashBoardService.getEmployeesCount());
        stats.put("totalCustomers", dashBoardService.getCustomersCount());
        stats.put("totalOrders", dashBoardService.getOrdersCount());
        stats.put("totalProducts", dashBoardService.getProductsCount());
        stats.put("totalPayments", dashBoardService.getPaymentsCount());
        stats.put("totalSalesAmount", dashBoardService.getTotalSalesAmount());
        stats.put("pendingOrdersCount", dashBoardService.getPendingOrdersCount());
        return ResponseEntity.ok(stats);
    }

    // GET /api/v1/dashboard/recent-orders
    // Returns top 5 most recent orders
    @GetMapping("/recent-orders")
    public ResponseEntity<List<RecentOrderView>> getRecentOrders() {
        return ResponseEntity.ok(dashBoardService.getRecentOrders());
    }

    // GET /api/v1/dashboard/orders-per-month
    // Returns map of month name -> order count
    @GetMapping("/orders-per-month")
    public ResponseEntity<Map<String, Long>> getOrdersPerMonth() {
        return ResponseEntity.ok(dashBoardService.getOrdersPerMonth());
    }
}
