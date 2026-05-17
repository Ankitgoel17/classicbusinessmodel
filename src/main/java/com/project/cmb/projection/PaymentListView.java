package com.project.cmb.projection;

import org.springframework.data.rest.core.config.Projection;
import com.project.cmb.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;

@Projection(name = "paymentList", types = { Payment.class })
public interface PaymentListView {

    // Composite key fields
    PaymentIdInfo getId();
    interface PaymentIdInfo {
        String getCheckNumber();
        Integer getCustomerNumber();
    }

    Integer getOrderNumber();
    LocalDate getPaymentDate();
    BigDecimal getAmount();

    // Customer name — fetched via customerNumber
    // handled in controller/service since Payment has no direct Customer relationship
}
