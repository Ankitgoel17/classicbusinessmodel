package com.project.cmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cmb.entity.Order;
import com.project.cmb.projection.OrderListView;
import com.project.cmb.repo.OrderRepo;
import com.project.cmb.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    OrderRepo orderRepo;

    @MockBean
    OrderService orderService;

    private OrderListView buildOrderView(
            Integer orderNumber,
            String status,
            Integer customerNumber,
            String customerName) {

        return new OrderListView() {

            public Integer getOrderNumber() {
                return orderNumber;
            }

            public LocalDate getOrderDate() {
                return LocalDate.of(2024, 1, 10);
            }

            public LocalDate getRequiredDate() {
                return LocalDate.of(2024, 1, 20);
            }

            public LocalDate getShippedDate() {
                return null;
            }

            public String getStatus() {
                return status;
            }

            public CustomerInfo getCustomer() {
                return new CustomerInfo() {

                    public Integer getCustomerNumber() {
                        return customerNumber;
                    }

                    public String getCustomerName() {
                        return customerName;
                    }
                };
            }
        };
    }

    private Order buildOrder(
            Integer orderNumber,
            String status) {

        Order o = new Order();

        o.setOrderNumber(orderNumber);
        o.setOrderDate(LocalDate.of(2024,1,10));
        o.setRequiredDate(LocalDate.of(2024,1,20));
        o.setStatus(status);

        return o;
    }

    @Test
    void getStats_shouldReturn200AndFields() throws Exception {

        when(orderService.getTotalOrders()).thenReturn(326L);
        when(orderService.getTotalShipped()).thenReturn(303L);
        when(orderService.getTotalInProcess()).thenReturn(6L);
        when(orderService.getTotalCancelled()).thenReturn(6L);

        mockMvc.perform(get("/api/v1/orders/stats"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalOrders").value(326))
                .andExpect(jsonPath("$.totalShipped").value(303))
                .andExpect(jsonPath("$.totalInProcess").value(6))
                .andExpect(jsonPath("$.totalCancelled").value(6));
    }

    @Test
    void filterByStatus_shouldReturn200AndList() throws Exception {

        OrderListView o =
                buildOrderView(
                        10100,
                        "Shipped",
                        363,
                        "Online Diecast Creations Co."
                );

        when(orderRepo.findByStatus("Shipped"))
                .thenReturn(List.of(o));

        mockMvc.perform(
                        get("/api/v1/orders/filter/status")
                                .param("status","Shipped"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber")
                        .value(10100))
                .andExpect(jsonPath("$[0].status")
                        .value("Shipped"));
    }

    @Test
    void filterByStatus_noMatch_shouldReturnEmptyList()
            throws Exception {

        when(orderRepo.findByStatus("NonExistent"))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/orders/filter/status")
                                .param("status","NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(0));
    }

    @Test
    void filterByDate_shouldReturn200AndList()
            throws Exception {

        OrderListView o =
                buildOrderView(
                        10100,
                        "Shipped",
                        363,
                        "Online Diecast Creations Co."
                );

        when(orderRepo.findByOrderDateBetween(
                eq(LocalDate.of(2024,1,1)),
                eq(LocalDate.of(2024,12,31))
        )).thenReturn(List.of(o));

        mockMvc.perform(
                        get("/api/v1/orders/filter/date")
                                .param("start","2024-01-01")
                                .param("end","2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber")
                        .value(10100));
    }

    @Test
    void searchByCustomerName_shouldReturn200AndPage()
            throws Exception {

        OrderListView o =
                buildOrderView(
                        10100,
                        "Shipped",
                        363,
                        "Online Diecast Creations Co."
                );

        when(orderRepo
                .findByCustomer_CustomerNameContainingIgnoreCase(
                        eq("online"),
                        any()))
                .thenReturn(
                        new PageImpl<>(
                                List.of(o),
                                PageRequest.of(0,10),
                                1
                        )
                );

        mockMvc.perform(
                        get("/api/v1/orders/search")
                                .param("customerName","online"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.content[0].customer.customerName")
                        .value("Online Diecast Creations Co."));
    }

    @Test
    void getFinancials_shouldReturn200AndAllFields()
            throws Exception {

        when(orderService.getOrderTotal(10100))
                .thenReturn(new BigDecimal("1000.00"));

        when(orderService.getTotalPaymentReceived(10100))
                .thenReturn(new BigDecimal("800.00"));

        when(orderService.getPendingPayment(10100))
                .thenReturn(new BigDecimal("200.00"));

        mockMvc.perform(
                        get("/api/v1/orders/10100/financials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderTotal")
                        .value(1000.00))
                .andExpect(jsonPath("$.pendingPayment")
                        .value(200.00));
    }

    @Test
    void createOrder_shouldReturn200AndOrder()
            throws Exception {

        Order savedOrder =
                buildOrder(99999,"In Process");

        when(orderService.createOrder(any(),any()))
                .thenReturn(savedOrder);

        Map<String,Object> requestBody =
                Map.of(
                        "order",
                        Map.of(
                                "orderNumber",99999,
                                "orderDate","2024-01-15",
                                "requiredDate","2024-01-25",
                                "status","In Process"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/orders/create")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        requestBody
                                                )
                                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber")
                        .value(99999));
    }
}