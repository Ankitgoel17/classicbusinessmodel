package com.project.cmb.service;

import com.project.cmb.constant.OrderStatus;
import com.project.cmb.dto.RecentOrderDto;
import com.project.cmb.entity.OrderDetail;
import com.project.cmb.entity.Payment;
import com.project.cmb.projection.RecentOrderView;
import com.project.cmb.repo.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DashBoardService {

    private final EmployeeRepo    employeeRepo;
    private final CustomerRepo    customerRepo;
    private final OrderRepo       orderRepo;
    private final OrderDetailRepo orderDetailRepo;
    private final ProductRepo     productRepo;
    private final PaymentRepo     paymentRepo;

    @Transactional(readOnly = true)
    public long getEmployeesCount()  { return employeeRepo.count(); }

    @Transactional(readOnly = true)
    public long getCustomersCount()  { return customerRepo.count(); }

    @Transactional(readOnly = true)
    public long getOrdersCount()     { return orderRepo.count(); }

    @Transactional(readOnly = true)
    public long getProductsCount()   { return productRepo.count(); }

    @Transactional(readOnly = true)
    public long getPaymentsCount()   { return paymentRepo.count(); }

    @Transactional(readOnly = true)
    public BigDecimal getTotalSalesAmount() {
        return paymentRepo.findAll()
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns top 5 recent orders with a pre-computed orderTotal
     * (sum of quantityOrdered * priceEach from orderdetails).
     */
    @Transactional(readOnly = true)
    public List<RecentOrderDto> getRecentOrders() {
        List<RecentOrderView> views = orderRepo.findTop5ByOrderByOrderDateDesc();
        List<RecentOrderDto> result = new ArrayList<>();
        for (RecentOrderView v : views) {
            List<OrderDetail> details = orderDetailRepo.findById_OrderNumber(v.getOrderNumber());
            BigDecimal total = details.stream()
                    .map(d -> d.getPriceEach().multiply(BigDecimal.valueOf(d.getQuantityOrdered())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            RecentOrderDto dto = new RecentOrderDto();
            dto.setOrderNumber(v.getOrderNumber());
            dto.setOrderDate(v.getOrderDate());
            dto.setStatus(v.getStatus());
            dto.setOrderTotal(total);

            if (v.getCustomer() != null) {
                RecentOrderDto.CustomerInfo ci = new RecentOrderDto.CustomerInfo();
                ci.setCustomerNumber(v.getCustomer().getCustomerNumber());
                ci.setCustomerName(v.getCustomer().getCustomerName());
                dto.setCustomer(ci);
            }
            result.add(dto);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public long getPendingOrdersCount() {
        return orderRepo.countByStatus(OrderStatus.IN_PROCESS);
    }

    /** Returns orders grouped by month name (Jan–Dec), all 12 months always present. */
    @Transactional(readOnly = true)
    public Map<String, Long> getOrdersPerMonth() {
        Map<Integer, Long> byOrdinal = orderRepo.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrderDate().getMonthValue(),
                        Collectors.counting()
                ));

        Map<String, Long> ordered = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            String abbr = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            ordered.put(abbr, byOrdinal.getOrDefault(m, 0L));
        }
        return ordered;
    }
}