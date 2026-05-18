package com.project.cmb.service;

import com.project.cmb.entity.*;
import com.project.cmb.exception.InsufficientStockException;
import com.project.cmb.exception.ResourceNotFoundException;
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

    @Mock private OrderRepo orderRepo;
    @Mock private OrderDetailRepo orderDetailRepo;
    @Mock private ProductRepo productRepo;
    @Mock private PaymentRepo paymentRepo;

    @InjectMocks private OrderService orderService;

    private Order order;
    private OrderDetail od1, od2;
    private Product product1, product2;

    // PaymentListView is an interface — mock it
    private PaymentListView paymentView1, paymentView2;

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

        OrderDetailId id1 = new OrderDetailId(90001, "S18_1749");
        od1 = new OrderDetail();
        od1.setId(id1); od1.setQuantityOrdered(10);
        od1.setPriceEach(new BigDecimal("136.00")); od1.setOrderLineNumber((short) 1);

        OrderDetailId id2 = new OrderDetailId(90001, "S18_2248");
        od2 = new OrderDetail();
        od2.setId(id2); od2.setQuantityOrdered(5);
        od2.setPriceEach(new BigDecimal("55.09")); od2.setOrderLineNumber((short) 2);

        // Mock PaymentListView — interface, can't instantiate directly
        paymentView1 = mock(PaymentListView.class);
        when(paymentView1.getAmount()).thenReturn(new BigDecimal("1000.00"));

        paymentView2 = mock(PaymentListView.class);
        when(paymentView2.getAmount()).thenReturn(new BigDecimal("360.45"));
    }

    // --- getTotalOrders ---

    @Test
    void getTotalOrders_shouldReturnCount() {
        when(orderRepo.count()).thenReturn(10L);
        assertThat(orderService.getTotalOrders()).isEqualTo(10L);
        verify(orderRepo).count();
    }

    // --- getTotalShipped ---

    @Test
    void getTotalShipped_shouldReturnCountByStatus() {
        when(orderRepo.countByStatus("Shipped")).thenReturn(4L);
        assertThat(orderService.getTotalShipped()).isEqualTo(4L);
        verify(orderRepo).countByStatus("Shipped");
    }

    // --- getTotalInProcess ---

    @Test
    void getTotalInProcess_shouldReturnCountByStatus() {
        when(orderRepo.countByStatus("In Process")).thenReturn(3L);
        assertThat(orderService.getTotalInProcess()).isEqualTo(3L);
        verify(orderRepo).countByStatus("In Process");
    }

    // --- getTotalCancelled ---

    @Test
    void getTotalCancelled_shouldReturnCountByStatus() {
        when(orderRepo.countByStatus("Cancelled")).thenReturn(2L);
        assertThat(orderService.getTotalCancelled()).isEqualTo(2L);
        verify(orderRepo).countByStatus("Cancelled");
    }

    // --- getOrderTotal ---

    @Test
    void getOrderTotal_shouldReturnSumOfLineItems() {
        // od1: 10 * 136.00 = 1360.00
        // od2:  5 *  55.09 =  275.45
        // total = 1635.45
        when(orderDetailRepo.findById_OrderNumber(90001))
                .thenReturn(List.of(od1, od2));

        BigDecimal result = orderService.getOrderTotal(90001);

        assertThat(result).isEqualByComparingTo("1635.45");
        verify(orderDetailRepo).findById_OrderNumber(90001);
    }

    @Test
    void getOrderTotal_noLineItems_shouldReturnZero() {
        when(orderDetailRepo.findById_OrderNumber(90001)).thenReturn(List.of());
        assertThat(orderService.getOrderTotal(90001))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- getTotalPaymentReceived ---

    @Test
    void getTotalPaymentReceived_shouldReturnSumOfPayments() {
        // 1000.00 + 360.45 = 1360.45
        when(paymentRepo.findByOrderNumber(90001))
                .thenReturn(List.of(paymentView1, paymentView2));

        BigDecimal result = orderService.getTotalPaymentReceived(90001);

        assertThat(result).isEqualByComparingTo("1360.45");
        verify(paymentRepo).findByOrderNumber(90001);
    }

    @Test
    void getTotalPaymentReceived_noPayments_shouldReturnZero() {
        when(paymentRepo.findByOrderNumber(90001)).thenReturn(List.of());
        assertThat(orderService.getTotalPaymentReceived(90001))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- getPendingPayment ---

    @Test
    void getPendingPayment_shouldReturnOrderTotalMinusPayments() {
        // order total = 1635.45, payments = 1360.45, pending = 275.00
        when(orderDetailRepo.findById_OrderNumber(90001))
                .thenReturn(List.of(od1, od2));
        when(paymentRepo.findByOrderNumber(90001))
                .thenReturn(List.of(paymentView1, paymentView2));

        BigDecimal result = orderService.getPendingPayment(90001);

        assertThat(result).isEqualByComparingTo("275.00");
    }

    @Test
    void getPendingPayment_fullyPaid_shouldReturnZero() {
        when(orderDetailRepo.findById_OrderNumber(90001))
                .thenReturn(List.of(od1)); // od1 total = 10 * 136.00 = 1360.00

        PaymentListView bigPayment = mock(PaymentListView.class);
        when(bigPayment.getAmount()).thenReturn(new BigDecimal("1360.00"));
        when(paymentRepo.findByOrderNumber(90001))
                .thenReturn(List.of(bigPayment));

        assertThat(orderService.getPendingPayment(90001))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- createOrder ---

    @Test
    void createOrder_shouldSaveOrderAndDecreaseStock() {
        when(productRepo.findById("S18_1749")).thenReturn(Optional.of(product1));
        when(productRepo.findById("S18_2248")).thenReturn(Optional.of(product2));
        when(orderRepo.save(order)).thenReturn(order);

        Order result = orderService.createOrder(order, List.of(od1, od2));

        assertThat(result.getOrderNumber()).isEqualTo(90001);
        assertThat(product1.getQuantityInStock()).isEqualTo((short) 90); // 100 - 10
        assertThat(product2.getQuantityInStock()).isEqualTo((short) 45); // 50 - 5

        verify(orderRepo).save(order);
        verify(orderDetailRepo).save(od1);
        verify(orderDetailRepo).save(od2);
        verify(productRepo, times(2)).save(any(Product.class));
    }

    @Test
    void createOrder_insufficientStock_shouldThrowException() {
        od1.setQuantityOrdered(200);
        when(productRepo.findById("S18_1749")).thenReturn(Optional.of(product1));

        assertThatThrownBy(() -> orderService.createOrder(order, List.of(od1)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepo, never()).save(any());
    }

    @Test
    void createOrder_productNotFound_shouldThrowException() {
        when(productRepo.findById("S18_1749")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(order, List.of(od1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");

        verify(orderRepo, never()).save(any());
    }
}