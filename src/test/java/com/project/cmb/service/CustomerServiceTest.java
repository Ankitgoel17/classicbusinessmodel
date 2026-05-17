package com.project.cmb.service;

import com.project.cmb.entity.Customer;
import com.project.cmb.entity.Order;
import com.project.cmb.entity.Payment;
import com.project.cmb.entity.PaymentId;
import com.project.cmb.projection.OrderListView;
import com.project.cmb.projection.PaymentListView;
import com.project.cmb.repo.CustomerRepo;
import com.project.cmb.repo.OrderRepo;
import com.project.cmb.repo.PaymentRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerServiceTest {

    @Mock private CustomerRepo customerRepo;
    @Mock private OrderRepo orderRepo;
    @Mock private PaymentRepo paymentRepo;

    @InjectMocks private CustomerService customerService;

    private Customer c1, c2, c3;
    private OrderListView orderView1, orderView2;
    private PaymentListView paymentView1, paymentView2;

    @BeforeEach
    void setup() {
        c1 = new Customer();
        c1.setCustomerNumber(101); c1.setCustomerName("Alpha Corp");
        c1.setCountry("USA"); c1.setCreditLimit(new BigDecimal("10000.00"));

        c2 = new Customer();
        c2.setCustomerNumber(102); c2.setCustomerName("Beta Ltd");
        c2.setCountry("France"); c2.setCreditLimit(new BigDecimal("20000.00"));

        c3 = new Customer();
        c3.setCustomerNumber(103); c3.setCustomerName("Gamma Inc");
        c3.setCountry("USA"); c3.setCreditLimit(new BigDecimal("30000.00"));

        orderView1 = mock(OrderListView.class);
        when(orderView1.getOrderNumber()).thenReturn(90001);
        when(orderView1.getStatus()).thenReturn("Shipped");

        orderView2 = mock(OrderListView.class);
        when(orderView2.getOrderNumber()).thenReturn(90002);
        when(orderView2.getStatus()).thenReturn("In Process");

        PaymentListView.PaymentIdInfo pid1 = mock(PaymentListView.PaymentIdInfo.class);
        when(pid1.getCheckNumber()).thenReturn("CHK001");
        when(pid1.getCustomerNumber()).thenReturn(101);
        paymentView1 = mock(PaymentListView.class);
        when(paymentView1.getId()).thenReturn(pid1);
        when(paymentView1.getAmount()).thenReturn(new BigDecimal("1500.00"));

        PaymentListView.PaymentIdInfo pid2 = mock(PaymentListView.PaymentIdInfo.class);
        when(pid2.getCheckNumber()).thenReturn("CHK002");
        when(pid2.getCustomerNumber()).thenReturn(101);
        paymentView2 = mock(PaymentListView.class);
        when(paymentView2.getId()).thenReturn(pid2);
        when(paymentView2.getAmount()).thenReturn(new BigDecimal("2500.00"));
    }

    @Test
    void getTotalCustomers_shouldReturnCount() {
        when(customerRepo.count()).thenReturn(3L);
        assertThat(customerService.getTotalCustomers()).isEqualTo(3L);
        verify(customerRepo).count();
    }

    @Test
    void getTotalCountries_shouldReturnDistinctCount() {
        when(customerRepo.findAll()).thenReturn(List.of(c1, c2, c3));
        assertThat(customerService.getTotalCountries()).isEqualTo(2L);
        verify(customerRepo).findAll();
    }

    @Test
    void getTotalCountries_emptyRepo_shouldReturnZero() {
        when(customerRepo.findAll()).thenReturn(List.of());
        assertThat(customerService.getTotalCountries()).isEqualTo(0L);
    }

    @Test
    void getTotalCountries_allSameCountry_shouldReturnOne() {
        when(customerRepo.findAll()).thenReturn(List.of(c1, c3));
        assertThat(customerService.getTotalCountries()).isEqualTo(1L);
    }

    @Test
    void getAvgCreditLimit_shouldReturnCorrectAverage() {
        when(customerRepo.findAll()).thenReturn(List.of(c1, c2, c3));
        assertThat(customerService.getAvgCreditLimit())
                .isEqualByComparingTo("20000.00");
        verify(customerRepo).findAll();
    }

    @Test
    void getAvgCreditLimit_singleCustomer_shouldReturnThatLimit() {
        when(customerRepo.findAll()).thenReturn(List.of(c1));
        assertThat(customerService.getAvgCreditLimit())
                .isEqualByComparingTo("10000.00");
    }

    @Test
    void getAvgCreditLimit_emptyRepo_shouldReturnZero() {
        when(customerRepo.findAll()).thenReturn(List.of());
        assertThat(customerService.getAvgCreditLimit())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }
}