package com.project.cmb.projection;

import com.project.cmb.entity.Order;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDate;

@Projection(name = "orderDetail", types = {Order.class})
public interface OrderDetailView {

    Integer getOrderNumber();

    LocalDate getOrderDate();

    LocalDate getRequiredDate();

    LocalDate getShippedDate();

    String getStatus();

    String getComments();

    CustomerInfo getCustomer();

    interface CustomerInfo {

        Integer getCustomerNumber();

        String getCustomerName();

        String getPhone();

        String getCity();

        String getCountry();
    }
}