package com.project.cmb.projection;

import org.springframework.data.rest.core.config.Projection;
import com.project.cmb.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;

@Projection(name = "paymentDetail", types = { Payment.class })
public interface PaymentDetailView {

    // Composite key fields
    PaymentIdInfo getId();
    interface PaymentIdInfo {
        String getCheckNumber();
        Integer getCustomerNumber();
    }

    Integer getOrderNumber();
    LocalDate getPaymentDate();
    BigDecimal getAmount();

    // Customer and order details fetched separately via:
    // GET /api/v1/payments/{customerNumber}/{checkNumber}/customer
    // GET /api/v1/payments/{customerNumber}/{checkNumber}/order
}
