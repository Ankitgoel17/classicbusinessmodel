package com.project.cmb.repo;

import com.project.cmb.entity.Payment;
import com.project.cmb.entity.PaymentId;
import com.project.cmb.projection.PaymentListView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDate;
import java.util.List;

@RepositoryRestResource(path = "payments")
public interface PaymentRepo extends JpaRepository<Payment, PaymentId> {

    List<PaymentListView> findById_CustomerNumber(Integer customerNumber);

    List<PaymentListView> findByOrderNumber(Integer orderNumber);

    List<PaymentListView> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);

    List<PaymentListView> findById_CheckNumberContainingIgnoreCase(String checkNumber);
}