package com.project.cmb.service;

import com.project.cmb.entity.Customer;
import com.project.cmb.entity.Order;
import com.project.cmb.entity.Payment;
import com.project.cmb.entity.PaymentId;
import com.project.cmb.repo.CustomerRepo;
import com.project.cmb.repo.OrderRepo;
import com.project.cmb.repo.PaymentRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import com.project.cmb.exception.ResourceNotFoundException;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepo paymentRepo;
    @Mock private CustomerRepo customerRepo;
    @Mock private OrderRepo orderRepo;

    @InjectMocks private PaymentService paymentService;

    private Payment p1, p2, p3;
    private Customer customer;
    private Order order;

    @BeforeEach
    void setup() {
        customer = new Customer();
        customer.setCustomerNumber(103);
        customer.setCustomerName("Atelier graphique");
        customer.setCountry("France");

        order = new Order();
        order.setOrderNumber(90001);
        order.setStatus("Shipped");
        order.setOrderDate(LocalDate.of(2024, 1, 10));
        order.setCustomer(customer);

        PaymentId pid1 = new PaymentId(103, "HQ336336");
        p1 = new Payment();
        p1.setId(pid1);
        p1.setAmount(new BigDecimal("6066.78"));
        p1.setPaymentDate(LocalDate.of(2024, 1, 10));
        p1.setOrderNumber(90001);

        PaymentId pid2 = new PaymentId(103, "JM555205");
        p2 = new Payment();
        p2.setId(pid2);
        p2.setAmount(new BigDecimal("14571.44"));
        p2.setPaymentDate(LocalDate.of(2024, 2, 15));
        p2.setOrderNumber(90001);

        PaymentId pid3 = new PaymentId(112, "OM314933");
        p3 = new Payment();
        p3.setId(pid3);
        p3.setAmount(new BigDecimal("1676.14"));
        p3.setPaymentDate(LocalDate.of(2024, 3, 20));
        p3.setOrderNumber(90002);
    }

    // --- getTotalPayments ---

    @Test
    void getTotalPayments_shouldReturnCount() {
        when(paymentRepo.count()).thenReturn(3L);
        assertThat(paymentService.getTotalPayments()).isEqualTo(3L);
        verify(paymentRepo).count();
    }

    // --- getTotalAmount ---

    @Test
    void getTotalAmount_shouldReturnSumOfAllPayments() {
        // 6066.78 + 14571.44 + 1676.14 = 22314.36
        when(paymentRepo.findAll()).thenReturn(List.of(p1, p2, p3));

        BigDecimal result = paymentService.getTotalAmount();

        assertThat(result).isEqualByComparingTo("22314.36");
        verify(paymentRepo).findAll();
    }

    @Test
    void getTotalAmount_singlePayment_shouldReturnThatAmount() {
        when(paymentRepo.findAll()).thenReturn(List.of(p1));
        assertThat(paymentService.getTotalAmount())
                .isEqualByComparingTo("6066.78");
    }

    @Test
    void getTotalAmount_noPayments_shouldReturnZero() {
        when(paymentRepo.findAll()).thenReturn(List.of());
        assertThat(paymentService.getTotalAmount())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- getCustomerByPayment ---

    @Test
    void getCustomerByPayment_shouldReturnCustomer() {
        when(customerRepo.findById(103)).thenReturn(Optional.of(customer));

        Customer result = paymentService.getCustomerByPayment(103);

        assertThat(result.getCustomerNumber()).isEqualTo(103);
        assertThat(result.getCustomerName()).isEqualTo("Atelier graphique");
        verify(customerRepo).findById(103);
    }

    @Test
    void getCustomerByPayment_notFound_shouldThrowException() {
        when(customerRepo.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getCustomerByPayment(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer");
    }

    // --- getOrderByPayment ---

    @Test
    void getOrderByPayment_shouldReturnOrder() {
        when(orderRepo.findById(90001)).thenReturn(Optional.of(order));

        Order result = paymentService.getOrderByPayment(90001);

        assertThat(result.getOrderNumber()).isEqualTo(90001);
        assertThat(result.getStatus()).isEqualTo("Shipped");
        verify(orderRepo).findById(90001);
    }

    @Test
    void getOrderByPayment_notFound_shouldThrowException() {
        when(orderRepo.findById(99999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getOrderByPayment(99999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order");
    }
}