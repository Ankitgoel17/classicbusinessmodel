package com.project.cmb.controller;

import com.project.cmb.entity.Customer;
import com.project.cmb.entity.Order;
import com.project.cmb.entity.Payment;
import com.project.cmb.entity.PaymentId;
import com.project.cmb.projection.PaymentListView;
import com.project.cmb.repo.PaymentRepo;
import com.project.cmb.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@AllArgsConstructor
public class PaymentController {

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
