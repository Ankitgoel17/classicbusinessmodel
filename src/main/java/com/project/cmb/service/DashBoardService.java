package com.project.cmb.service;
import com.project.cmb.entity.Payment;
import com.project.cmb.projection.RecentOrderView;
import com.project.cmb.repo.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DashBoardService {

    private final EmployeeRepo employeeRepo;
    private final CustomerRepo customerRepo;
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private final PaymentRepo paymentRepo;

    public long getEmployeesCount()  { return employeeRepo.count(); }
    public long getCustomersCount()  { return customerRepo.count(); }
    public long getOrdersCount()     { return orderRepo.count(); }
    public long getProductsCount()   { return productRepo.count(); }
    public long getPaymentsCount()   { return paymentRepo.count(); }

    public BigDecimal getTotalSalesAmount() {
        return paymentRepo.findAll()
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<RecentOrderView> getRecentOrders() {
        return orderRepo.findTop5ByOrderByOrderDateDesc();
    }

    public long getPendingOrdersCount() {
        return orderRepo.countByStatus("In Process");
    }

    public Map<String, Long> getOrdersPerMonth() {
        return orderRepo.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrderDate().getMonth().name(),
                        Collectors.counting()
                ));
    }
}