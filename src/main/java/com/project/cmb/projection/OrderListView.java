package com.project.cmb.projection;

import com.project.cmb.entity.Order;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDate;

@Projection(name = "orderList", types = {Order.class})
public interface OrderListView {

    Integer getOrderNumber();

    LocalDate getOrderDate();

    LocalDate getRequiredDate();

    LocalDate getShippedDate();

    String getStatus();

    CustomerInfo getCustomer();

    interface CustomerInfo {

        Integer getCustomerNumber();

        String getCustomerName();
    }
}