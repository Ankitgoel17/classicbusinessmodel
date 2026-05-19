package com.project.cmb.controller;

import com.project.cmb.entity.PaymentId;
import com.project.cmb.exception.ResourceNotFoundException;
import com.project.cmb.projection.PaymentDetailView;
import com.project.cmb.projection.PaymentListView;
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
@RequestMapping("/api/v1/payments")
@AllArgsConstructor
public class PaymentController {

    private final PaymentRepo    paymentRepo;
    private final PaymentService paymentService;

    // GET /api/v1/payments?page=0&size=10
    @GetMapping
    public ResponseEntity<Page<PaymentListView>> getAllPaged(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(paymentRepo.findAllProjectedBy(pageable));
    }

    // GET /api/v1/payments/{customerNumber}/{checkNumber}
    @GetMapping("/{customerNumber}/{checkNumber}")
    public ResponseEntity<PaymentDetailView> getPayment(
            @PathVariable Integer customerNumber,
            @PathVariable String checkNumber) {
        return paymentRepo.findPaymentDetailById(new PaymentId(customerNumber, checkNumber))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment", "id", customerNumber + "/" + checkNumber));
    }

    // GET /api/v1/payments/search/check?checkNumber=HQ336&page=0&size=10
    @GetMapping("/search/check")
    public ResponseEntity<Page<PaymentListView>> searchByCheckNumber(
            @RequestParam String checkNumber,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                paymentRepo.findById_CheckNumberContainingIgnoreCase(checkNumber, pageable));
    }

    // GET /api/v1/payments/stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPayments", paymentService.getTotalPayments());
        stats.put("totalAmount",   paymentService.getTotalAmount());
        return ResponseEntity.ok(stats);
    }

    // GET /api/v1/payments/customer/{customerNumber}
    @GetMapping("/customer/{customerNumber}")
    public ResponseEntity<List<PaymentListView>> getByCustomer(
            @PathVariable Integer customerNumber) {
        return ResponseEntity.ok(paymentRepo.findById_CustomerNumber(customerNumber));
    }

    // GET /api/v1/payments/order/{orderNumber}
    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<List<PaymentListView>> getByOrder(
            @PathVariable Integer orderNumber) {
        return ResponseEntity.ok(paymentRepo.findByOrderNumber(orderNumber));
    }

    // GET /api/v1/payments/filter/date?start=2024-01-01&end=2024-12-31
    @GetMapping("/filter/date")
    public ResponseEntity<List<PaymentListView>> filterByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(paymentRepo.findByPaymentDateBetween(start, end));
    }

    // PUT /api/v1/payments/{customerNumber}/{checkNumber}/amount
    @PutMapping("/{customerNumber}/{checkNumber}/amount")
    public ResponseEntity<Void> updateAmount(
            @PathVariable Integer customerNumber,
            @PathVariable String checkNumber,
            @RequestParam BigDecimal amount) {
        paymentRepo.findById(new PaymentId(customerNumber, checkNumber))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment", "id", customerNumber + "/" + checkNumber));
        paymentRepo.findById(new PaymentId(customerNumber, checkNumber))
                .ifPresent(p -> {
                    p.setAmount(amount);
                    paymentRepo.save(p);
                });
        return ResponseEntity.noContent().build();
    }
}