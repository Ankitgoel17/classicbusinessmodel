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
import java.math.RoundingMode;
import java.util.List;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepo customerRepo;
    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;

    public long getTotalCustomers() {
        return customerRepo.count();
    }

    public long getTotalCountries() {
        return customerRepo.findAll()
                .stream()
                .map(Customer::getCountry)
                .distinct()
                .count();
    }

    public BigDecimal getAvgCreditLimit() {
        List<Customer> all = customerRepo.findAll();
        if (all.isEmpty()) return BigDecimal.ZERO;
        BigDecimal total = all.stream()
                .map(Customer::getCreditLimit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(new BigDecimal(all.size()), 2, RoundingMode.HALF_UP);
    }
}