package com.project.cmb.controller;

import com.project.cmb.entity.Customer;
import com.project.cmb.entity.Order;
import com.project.cmb.entity.Payment;
import com.project.cmb.entity.PaymentId;
import com.project.cmb.projection.PaymentListView;
import com.project.cmb.repo.PaymentRepo;
import com.project.cmb.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean PaymentRepo paymentRepo;
    @MockBean PaymentService paymentService;

    // --- helpers ---

    private PaymentListView buildPaymentView(String checkNumber,
                                             Integer customerNumber, Integer orderNumber, BigDecimal amount) {
        return new PaymentListView() {
            public PaymentIdInfo getId() {
                return new PaymentIdInfo() {
                    public String getCheckNumber() { return checkNumber; }
                    public Integer getCustomerNumber() { return customerNumber; }
                };
            }
            public Integer getOrderNumber() { return orderNumber; }
            public LocalDate getPaymentDate() { return LocalDate.of(2024, 1, 10); }
            public BigDecimal getAmount() { return amount; }
        };
    }

    private Payment buildPayment(Integer customerNumber, String checkNumber,
                                 Integer orderNumber, BigDecimal amount) {
        Payment p = new Payment();
        p.setId(new PaymentId(customerNumber, checkNumber));
        p.setOrderNumber(orderNumber);
        p.setPaymentDate(LocalDate.of(2024, 1, 10));
        p.setAmount(amount);
        return p;
    }

    // --- GET /api/v1/payments/stats ---

    @Test
    void getStats_shouldReturn200AndFields() throws Exception {
        when(paymentService.getTotalPayments()).thenReturn(273L);
        when(paymentService.getTotalAmount()).thenReturn(new BigDecimal("8853839.23"));

        mockMvc.perform(get("/api/v1/payments/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalPayments").value(273))
                .andExpect(jsonPath("$.totalAmount").exists());
    }

    // --- GET /api/v1/payments/customer/{customerNumber} ---

    @Test
    void getByCustomer_shouldReturn200AndList() throws Exception {
        PaymentListView p = buildPaymentView("HQ336336", 103, 10100,
                new BigDecimal("6066.78"));

        when(paymentRepo.findById_CustomerNumber(103)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/payments/customer/103"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id.checkNumber").value("HQ336336"))
                .andExpect(jsonPath("$[0].id.customerNumber").value(103))
                .andExpect(jsonPath("$[0].amount").value(6066.78));
    }

    @Test
    void getByCustomer_noMatch_shouldReturnEmptyList() throws Exception {
        when(paymentRepo.findById_CustomerNumber(99999)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/payments/customer/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- GET /api/v1/payments/order/{orderNumber} ---

    @Test
    void getByOrder_shouldReturn200AndList() throws Exception {
        PaymentListView p = buildPaymentView("HQ336336", 103, 10100,
                new BigDecimal("6066.78"));

        when(paymentRepo.findByOrderNumber(10100)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/payments/order/10100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].orderNumber").value(10100));
    }

    @Test
    void getByOrder_noMatch_shouldReturnEmptyList() throws Exception {
        when(paymentRepo.findByOrderNumber(99999)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/payments/order/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- GET /api/v1/payments/search/check?checkNumber=HQ336 ---

    @Test
    void searchByCheckNumber_shouldReturn200AndList() throws Exception {
        PaymentListView p = buildPaymentView("HQ336336", 103, 10100,
                new BigDecimal("6066.78"));

        when(paymentRepo.findById_CheckNumberContainingIgnoreCase("HQ336"))
                .thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/payments/search/check")
                        .param("checkNumber", "HQ336"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id.checkNumber").value("HQ336336"));
    }

    @Test
    void searchByCheckNumber_noMatch_shouldReturnEmptyList() throws Exception {
        when(paymentRepo.findById_CheckNumberContainingIgnoreCase("ZZZZZZ"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/payments/search/check")
                        .param("checkNumber", "ZZZZZZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- GET /api/v1/payments/filter/date?start=2024-01-01&end=2024-12-31 ---

    @Test
    void filterByDate_shouldReturn200AndList() throws Exception {
        PaymentListView p = buildPaymentView("HQ336336", 103, 10100,
                new BigDecimal("6066.78"));

        when(paymentRepo.findByPaymentDateBetween(
                eq(LocalDate.of(2024, 1, 1)),
                eq(LocalDate.of(2024, 12, 31))))
                .thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/payments/filter/date")
                        .param("start", "2024-01-01")
                        .param("end", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id.checkNumber").value("HQ336336"));
    }

    // --- GET /api/v1/payments/{customerNumber}/{checkNumber}/customer ---

    @Test
    void getLinkedCustomer_shouldReturn200AndCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerNumber(103);
        customer.setCustomerName("Atelier graphique");
        customer.setCountry("France");

        when(paymentService.getCustomerByPayment(103)).thenReturn(customer);

        mockMvc.perform(get("/api/v1/payments/103/HQ336336/customer"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerNumber").value(103))
                .andExpect(jsonPath("$.customerName").value("Atelier graphique"))
                .andExpect(jsonPath("$.country").value("France"));
    }

    // --- GET /api/v1/payments/{customerNumber}/{checkNumber}/order ---

    @Test
    void getLinkedOrder_shouldReturn200AndOrder() throws Exception {
        Payment payment = buildPayment(103, "HQ336336", 10100,
                new BigDecimal("6066.78"));

        Order order = new Order();
        order.setOrderNumber(10100);
        order.setStatus("Shipped");
        order.setOrderDate(LocalDate.of(2003, 1, 6));

        when(paymentRepo.findById(new PaymentId(103, "HQ336336")))
                .thenReturn(Optional.of(payment));
        when(paymentService.getOrderByPayment(10100)).thenReturn(order);

        mockMvc.perform(get("/api/v1/payments/103/HQ336336/order"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderNumber").value(10100))
                .andExpect(jsonPath("$.status").value("Shipped"));
    }

    @Test
    void updateAmount_shouldReturn200AndUpdatedPayment() throws Exception {
        Payment payment = buildPayment(103, "HQ336336", 10100,
                new BigDecimal("6066.78"));
        Payment updated = buildPayment(103, "HQ336336", 10100,
                new BigDecimal("7000.00"));

        when(paymentRepo.findById(new PaymentId(103, "HQ336336")))
                .thenReturn(Optional.of(payment));
        when(paymentRepo.save(any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/payments/103/HQ336336/amount/7000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(7000.00));
    }


}