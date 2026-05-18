package com.project.cmb.controller;

import com.project.cmb.entity.*;
import com.project.cmb.projection.EmployeeListView;
import com.project.cmb.projection.OfficeListView;
import com.project.cmb.projection.PaymentListView;
import com.project.cmb.repo.EmployeeRepo;
import com.project.cmb.repo.OfficeRepo;
import com.project.cmb.repo.PaymentRepo;
import com.project.cmb.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/offices")
@AllArgsConstructor
public class OfficeController {

    private final OfficeRepo officeRepo;
    private final EmployeeRepo employeeRepo;

    // GET /api/v1/offices/stats
    // Total offices and total employees
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOffices", officeRepo.count());
        stats.put("totalEmployees", employeeRepo.count());
        return ResponseEntity.ok(stats);
    }

    // GET /api/v1/offices/filter/country?countries=USA&countries=France
    // Filter offices by one or more countries
    @GetMapping("/filter/country")
    public ResponseEntity<List<OfficeListView>> filterByCountry(
            @RequestParam List<String> countries) {
        return ResponseEntity.ok(officeRepo.findByCountryIn(countries));
    }

    // GET /api/v1/offices/{officeCode}/employees?page=0&size=10
    // Get all employees in a specific office
    @GetMapping("/{officeCode}/employees")
    public ResponseEntity<Page<EmployeeListView>> getEmployees(
            @PathVariable String officeCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                employeeRepo.findByOffice_OfficeCode(officeCode, pageable));
    }

    // PUT /api/v1/offices/{officeCode}/phone/{phone}
    // Update office phone number only
    @PutMapping("/{officeCode}/phone/{phone}")
    public ResponseEntity<Office> updatePhone(
            @PathVariable String officeCode,
            @PathVariable String phone) {
        Office office = officeRepo.findById(officeCode)
                .orElseThrow(() -> new RuntimeException(
                        "Office not found: " + officeCode));
        office.setPhone(phone);
        return ResponseEntity.ok(officeRepo.save(office));
    }

    @RestController
    @RequestMapping("/api/v1/payments")
    @AllArgsConstructor
    public static class PaymentController {

        private final PaymentRepo paymentRepo;
        private final PaymentService paymentService;

        // GET /api/v1/payments/stats
        // Total payments count and total amount
        @GetMapping("/stats")
        public ResponseEntity<Map<String, Object>> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPayments", paymentService.getTotalPayments());
            stats.put("totalAmount", paymentService.getTotalAmount());
            return ResponseEntity.ok(stats);
        }

        // GET /api/v1/payments/customer/{customerNumber}
        // All payments for a specific customer
        @GetMapping("/customer/{customerNumber}")
        public ResponseEntity<List<PaymentListView>> getByCustomer(
                @PathVariable Integer customerNumber) {
            return ResponseEntity.ok(
                    paymentRepo.findById_CustomerNumber(customerNumber));
        }

        // GET /api/v1/payments/order/{orderNumber}
        // All payments linked to a specific order
        @GetMapping("/order/{orderNumber}")
        public ResponseEntity<List<PaymentListView>> getByOrder(
                @PathVariable Integer orderNumber) {
            return ResponseEntity.ok(
                    paymentRepo.findByOrderNumber(orderNumber));
        }

        // GET /api/v1/payments/search/check?checkNumber=HQ336
        // Search by check number
        @GetMapping("/search/check")
        public ResponseEntity<List<PaymentListView>> searchByCheckNumber(
                @RequestParam String checkNumber) {
            return ResponseEntity.ok(
                    paymentRepo.findById_CheckNumberContainingIgnoreCase(checkNumber));
        }

        // GET /api/v1/payments/filter/date?start=2024-01-01&end=2024-12-31
        // Filter payments by date range
        @GetMapping("/filter/date")
        public ResponseEntity<List<PaymentListView>> filterByDate(
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
            return ResponseEntity.ok(
                    paymentRepo.findByPaymentDateBetween(start, end));
        }

        // GET /api/v1/payments/{customerNumber}/{checkNumber}/customer
        // Customer details linked with this payment
        @GetMapping("/{customerNumber}/{checkNumber}/customer")
        public ResponseEntity<Customer> getLinkedCustomer(
                @PathVariable Integer customerNumber) {
            return ResponseEntity.ok(
                    paymentService.getCustomerByPayment(customerNumber));
        }

        // GET /api/v1/payments/{customerNumber}/{checkNumber}/order
        // Order details linked with this payment
        @GetMapping("/{customerNumber}/{checkNumber}/order")
        public ResponseEntity<Order> getLinkedOrder(
                @PathVariable Integer customerNumber,
                @PathVariable String checkNumber) {
            Payment payment = paymentRepo
                    .findById(new PaymentId(customerNumber, checkNumber))
                    .orElseThrow(() -> new RuntimeException(
                            "Payment not found: " + customerNumber + "/" + checkNumber));
            return ResponseEntity.ok(
                    paymentService.getOrderByPayment(payment.getOrderNumber()));
        }

        // PUT /api/v1/payments/{customerNumber}/{checkNumber}/amount/{amount}
        // Update payment amount only
        @PutMapping("/{customerNumber}/{checkNumber}/amount/{amount}")
        public ResponseEntity<Payment> updateAmount(
                @PathVariable Integer customerNumber,
                @PathVariable String checkNumber,
                @PathVariable BigDecimal amount) {
            Payment payment = paymentRepo
                    .findById(new PaymentId(customerNumber, checkNumber))
                    .orElseThrow(() -> new RuntimeException(
                            "Payment not found: " + customerNumber + "/" + checkNumber));
            payment.setAmount(amount);
            return ResponseEntity.ok(paymentRepo.save(payment));
        }
    }
}