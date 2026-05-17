package com.project.cmb.service;

import com.project.cmb.entity.Order;
import com.project.cmb.entity.Payment;
import com.project.cmb.projection.RecentOrderView;
import com.project.cmb.repo.CustomerRepo;
import com.project.cmb.repo.EmployeeRepo;
import com.project.cmb.repo.OrderRepo;
import com.project.cmb.repo.PaymentRepo;
import com.project.cmb.repo.ProductRepo;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashBoardServiceTest {

    @Mock private EmployeeRepo employeeRepo;
    @Mock private CustomerRepo customerRepo;
    @Mock private OrderRepo orderRepo;
    @Mock private ProductRepo productRepo;
    @Mock private PaymentRepo paymentRepo;

    @InjectMocks private DashBoardService dashBoardService;


    @Test
    void getEmployeesCount_shouldReturnCount() {
        when(employeeRepo.count()).thenReturn(5L);
        assertThat(dashBoardService.getEmployeesCount()).isEqualTo(5L);
        verify(employeeRepo).count();
    }


    @Test
    void getCustomersCount_shouldReturnCount() {
        when(customerRepo.count()).thenReturn(10L);
        assertThat(dashBoardService.getCustomersCount()).isEqualTo(10L);
        verify(customerRepo).count();
    }


    @Test
    void getOrdersCount_shouldReturnCount() {
        when(orderRepo.count()).thenReturn(12L);
        assertThat(dashBoardService.getOrdersCount()).isEqualTo(12L);
        verify(orderRepo).count();
    }


    @Test
    void getProductsCount_shouldReturnCount() {
        when(productRepo.count()).thenReturn(8L);
        assertThat(dashBoardService.getProductsCount()).isEqualTo(8L);
        verify(productRepo).count();
    }


    @Test
    void getPaymentsCount_shouldReturnCount() {
        when(paymentRepo.count()).thenReturn(20L);
        assertThat(dashBoardService.getPaymentsCount()).isEqualTo(20L);
        verify(paymentRepo).count();
    }


    @Test
    void getTotalSalesAmount_shouldReturnSumOfAllPayments() {
        Payment p1 = new Payment();
        p1.setAmount(new BigDecimal("1500.00"));
        Payment p2 = new Payment();
        p2.setAmount(new BigDecimal("2500.00"));

        when(paymentRepo.findAll()).thenReturn(List.of(p1, p2));

        assertThat(dashBoardService.getTotalSalesAmount())
                .isEqualByComparingTo("4000.00");
        verify(paymentRepo).findAll();
    }

    @Test
    void getTotalSalesAmount_noPayments_shouldReturnZero() {
        when(paymentRepo.findAll()).thenReturn(List.of());
        assertThat(dashBoardService.getTotalSalesAmount())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void getRecentOrders_shouldReturnTop5() {
        // RecentOrderView is an interface — mock it
        RecentOrderView view1 = mock(RecentOrderView.class);
        when(view1.getOrderNumber()).thenReturn(90001);
        when(view1.getOrderDate()).thenReturn(LocalDate.of(2024, 3, 1));
        when(view1.getStatus()).thenReturn("Shipped");

        RecentOrderView view2 = mock(RecentOrderView.class);
        when(view2.getOrderNumber()).thenReturn(90002);
        when(view2.getOrderDate()).thenReturn(LocalDate.of(2024, 2, 1));
        when(view2.getStatus()).thenReturn("In Process");

        when(orderRepo.findTop5ByOrderByOrderDateDesc())
                .thenReturn(List.of(view1, view2));

        List<RecentOrderView> result = dashBoardService.getRecentOrders();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOrderNumber()).isEqualTo(90001);
        verify(orderRepo).findTop5ByOrderByOrderDateDesc();
    }


    @Test
    void getPendingOrdersCount_shouldReturnCountByStatus() {
        when(orderRepo.countByStatus("In Process")).thenReturn(3L);
        assertThat(dashBoardService.getPendingOrdersCount()).isEqualTo(3L);
        verify(orderRepo).countByStatus("In Process");
    }


    @Test
    void getOrdersPerMonth_shouldGroupByMonth() {
        Order o1 = buildOrder(101, LocalDate.of(2024, 1, 15));
        Order o2 = buildOrder(102, LocalDate.of(2024, 1, 28));
        Order o3 = buildOrder(103, LocalDate.of(2024, 3, 5));

        when(orderRepo.findAll()).thenReturn(List.of(o1, o2, o3));

        Map<String, Long> result = dashBoardService.getOrdersPerMonth();

        assertThat(result)
                .containsEntry("JANUARY", 2L)
                .containsEntry("MARCH", 1L)
                .hasSize(2);
        verify(orderRepo).findAll();
    }

    @Test
    void getOrdersPerMonth_noOrders_shouldReturnEmptyMap() {
        when(orderRepo.findAll()).thenReturn(List.of());
        assertThat(dashBoardService.getOrdersPerMonth()).isEmpty();
        verify(orderRepo).findAll();
    }


    private Order buildOrder(Integer orderNumber, LocalDate orderDate) {
        Order o = new Order();
        o.setOrderNumber(orderNumber);
        o.setOrderDate(orderDate);
        o.setRequiredDate(orderDate.plusDays(7));
        o.setStatus("In Process");
        return o;
    }
}