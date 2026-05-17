package com.project.cmb.projection;

import org.springframework.data.rest.core.config.Projection;
import com.project.cmb.entity.Order;

import java.time.LocalDate;

@Projection(name = "recentOrder", types = { Order.class })
public interface RecentOrderView {

    Integer getOrderNumber();
    LocalDate getOrderDate();
    String getStatus();

    // Customer name for the Customer column
    CustomerInfo getCustomer();
    interface CustomerInfo {
        Integer getCustomerNumber();
        String getCustomerName();
    }

    // Amount shown in dashboard recent orders table
    // fetched via OrderService.getOrderTotal(orderNumber)
    // not a direct field — handled in DashboardController
}