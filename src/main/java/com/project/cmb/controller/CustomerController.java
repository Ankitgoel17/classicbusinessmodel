package com.project.cmb.controller;

import com.project.cmb.entity.Customer;
import com.project.cmb.entity.Order;
import com.project.cmb.entity.Payment;
import com.project.cmb.projection.CustomerListView;
import com.project.cmb.projection.OrderListView;
import com.project.cmb.projection.PaymentListView;
import com.project.cmb.repo.CustomerRepo;
import com.project.cmb.repo.OrderRepo;
import com.project.cmb.repo.PaymentRepo;
import com.project.cmb.service.CustomerService;
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
@RequestMapping("/api/v1/customers")
@AllArgsConstructor
public class CustomerController {

    private final CustomerRepo customerRepo;
    private final CustomerService customerService;
    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;


    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", customerService.getTotalCustomers());
        stats.put("totalCountries", customerService.getTotalCountries());
        stats.put("avgCreditLimit", customerService.getAvgCreditLimit());
        return ResponseEntity.ok(stats);
    }


    @GetMapping("/search")
    public ResponseEntity<Page<CustomerListView>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                customerRepo.findByCustomerNameContainingIgnoreCase(name, pageable));
    }


    @GetMapping("/search/phone")
    public ResponseEntity<Page<CustomerListView>> searchByPhone(
            @RequestParam String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                customerRepo.findByPhoneContaining(phone, pageable));
    }


    @GetMapping("/filter/country")
    public ResponseEntity<Page<CustomerListView>> filterByCountry(
            @RequestParam String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                customerRepo.findByCountry(country, pageable));
    }


    @GetMapping("/filter/credit")
    public ResponseEntity<Page<CustomerListView>> filterByCreditLimit(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                customerRepo.findByCreditLimitBetween(min, max, pageable));
    }


    @GetMapping("/{customerNumber}/orders")
    public ResponseEntity<List<OrderListView>> getOrders(
            @PathVariable Integer customerNumber) {
        return ResponseEntity.ok(
                orderRepo.findByCustomer_CustomerNumber(customerNumber));
    }


    @GetMapping("/{customerNumber}/payments")
    public ResponseEntity<List<PaymentListView>> getPayments(
            @PathVariable Integer customerNumber) {
        return ResponseEntity.ok(
                paymentRepo.findById_CustomerNumber(customerNumber));
    }
}