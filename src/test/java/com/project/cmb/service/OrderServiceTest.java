package com.project.cmb.service;

import com.project.cmb.entity.*;
import com.project.cmb.projection.PaymentListView;
import com.project.cmb.repo.*;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private OrderDetailRepo orderDetailRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private PaymentRepo paymentRepo;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderDetail od1;
    private OrderDetail od2;
    private Product product1;
    private Product product2;

    private PaymentListView paymentView1;
    private PaymentListView paymentView2;

    @BeforeEach
    void setup() {

        order = new Order();
        order.setOrderNumber(90001);
        order.setOrderDate(LocalDate.of(2024, 1, 10));
        order.setStatus("In Process");

        product1 = new Product();
        product1.setProductCode("S18_1749");
        product1.setProductName("1917 Grand Touring Sedan");
        product1.setQuantityInStock((short) 100);
        product1.setBuyPrice(new BigDecimal("86.70"));
        product1.setMsrp(new BigDecimal("170.00"));

        product2 = new Product();
        product2.setProductCode("S18_2248");
        product2.setProductName("1911 Ford Town Car");
        product2.setQuantityInStock((short) 50);
        product2.setBuyPrice(new BigDecimal("60.54"));
        product2.setMsrp(new BigDecimal("117.44"));

        OrderDetailId id1 =
                new OrderDetailId(90001, "S18_1749");

        od1 = new OrderDetail();
        od1.setId(id1);
        od1.setQuantityOrdered(10);
        od1.setPriceEach(new BigDecimal("136.00"));
        od1.setOrderLineNumber((short) 1);

        OrderDetailId id2 =
                new OrderDetailId(90001, "S18_2248");

        od2 = new OrderDetail();
        od2.setId(id2);
        od2.setQuantityOrdered(5);
        od2.setPriceEach(new BigDecimal("55.09"));
        od2.setOrderLineNumber((short) 2);

        paymentView1 = mock(PaymentListView.class);
        when(paymentView1.getAmount())
                .thenReturn(new BigDecimal("1000.00"));

        paymentView2 = mock(PaymentListView.class);
        when(paymentView2.getAmount())
                .thenReturn(new BigDecimal("360.45"));
    }

    @Test
    void getTotalOrders_shouldReturnCount() {

        when(orderRepo.count()).thenReturn(10L);

        assertThat(orderService.getTotalOrders())
                .isEqualTo(10L);

        verify(orderRepo).count();
    }

    @Test
    void getTotalShipped_shouldReturnCountByStatus() {

        when(orderRepo.countByStatus("Shipped"))
                .thenReturn(4L);

        assertThat(orderService.getTotalShipped())
                .isEqualTo(4L);

        verify(orderRepo)
                .countByStatus("Shipped");
    }

    @Test
    void getTotalInProcess_shouldReturnCountByStatus() {

        when(orderRepo.countByStatus("In Process"))
                .thenReturn(3L);

        assertThat(orderService.getTotalInProcess())
                .isEqualTo(3L);

        verify(orderRepo)
                .countByStatus("In Process");
    }

    @Test
    void getTotalCancelled_shouldReturnCountByStatus() {

        when(orderRepo.countByStatus("Cancelled"))
                .thenReturn(2L);

        assertThat(orderService.getTotalCancelled())
                .isEqualTo(2L);

        verify(orderRepo)
                .countByStatus("Cancelled");
    }

    @Test
    void getOrderTotal_shouldReturnSumOfLineItems() {

        when(orderDetailRepo.findById_OrderNumber(90001))
                .thenReturn(List.of(od1, od2));

        BigDecimal result =
                orderService.getOrderTotal(90001);

        assertThat(result)
                .isEqualByComparingTo("1635.45");

        verify(orderDetailRepo)
                .findById_OrderNumber(90001);
    }

    @Test
    void getOrderTotal_noLineItems_shouldReturnZero() {

        when(orderDetailRepo.findById_OrderNumber(90001))
                .thenReturn(List.of());

        assertThat(orderService.getOrderTotal(90001))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getTotalPaymentReceived_shouldReturnSumOfPayments() {

        when(paymentRepo.findByOrderNumber(90001))
                .thenReturn(List.of(paymentView1, paymentView2));

        BigDecimal result =
                orderService.getTotalPaymentReceived(90001);

        assertThat(result)
                .isEqualByComparingTo("1360.45");

        verify(paymentRepo)
                .findByOrderNumber(90001);
    }

    @Test
    void getTotalPaymentReceived_noPayments_shouldReturnZero() {

        when(paymentRepo.findByOrderNumber(90001))
                .thenReturn(List.of());

        assertThat(orderService.getTotalPaymentReceived(90001))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }
}