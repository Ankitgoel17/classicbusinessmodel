package com.project.cmb.projection;

import org.springframework.data.rest.core.config.Projection;
import com.project.cmb.entity.Customer;

import java.math.BigDecimal;

@Projection(name = "customerDetail", types = { Customer.class })
public interface CustomerDetailView {

    Integer getCustomerNumber();
    String getCustomerName();
    String getContactFirstName();
    String getContactLastName();
    String getPhone();
    String getAddressLine1();
    String getAddressLine2();
    String getCity();
    String getState();
    String getPostalCode();
    String getCountry();
    BigDecimal getCreditLimit();

    SalesRepInfo getSalesRepEmployee();
    interface SalesRepInfo {
        Integer getEmployeeNumber();
        String getFirstName();
        String getLastName();
    }
}