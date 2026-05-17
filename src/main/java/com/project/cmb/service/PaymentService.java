package com.project.cmb.service;

import com.project.cmb.entity.Customer;
import com.project.cmb.entity.Order;
import com.project.cmb.entity.Payment;
import com.project.cmb.repo.CustomerRepo;
import com.project.cmb.repo.OrderRepo;
import com.project.cmb.repo.PaymentRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final CustomerRepo customerRepo;
    private final OrderRepo orderRepo;

    public long getTotalPayments() {
        return paymentRepo.count();
    }

    // Total amount — sum of all payments
    public BigDecimal getTotalAmount() {
        return paymentRepo.findAll()
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Customer linked with a payment
    public Customer getCustomerByPayment(Integer customerNumber) {
        return customerRepo.findById(customerNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Customer not found: " + customerNumber));
    }

    // Order linked with a payment
    public Order getOrderByPayment(Integer orderNumber) {
        return orderRepo.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Order not found: " + orderNumber));
    }
}