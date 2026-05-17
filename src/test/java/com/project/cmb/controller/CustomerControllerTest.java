package com.project.cmb.controller;

import com.project.cmb.projection.CustomerListView;
import com.project.cmb.projection.OrderListView;
import com.project.cmb.projection.PaymentListView;
import com.project.cmb.repo.CustomerRepo;
import com.project.cmb.repo.OrderRepo;
import com.project.cmb.repo.PaymentRepo;
import com.project.cmb.service.CustomerService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean CustomerRepo customerRepo;
    @MockBean OrderRepo orderRepo;
    @MockBean PaymentRepo paymentRepo;
    @MockBean CustomerService customerService;

    private CustomerListView buildCustomer(Integer number, String name,
                                           String city, String country, BigDecimal credit) {
        return new CustomerListView() {
            public Integer getCustomerNumber() { return number; }
            public String getContactFirstName() { return "John"; }
            public String getContactLastName() { return "Doe"; }
            public String getCity() { return city; }
            public String getCountry() { return country; }
            public BigDecimal getCreditLimit() { return credit; }
        };
    }

    private OrderListView buildOrder(Integer orderNumber, String status) {
        return new OrderListView() {
            public Integer getOrderNumber() { return orderNumber; }
            public LocalDate getOrderDate() { return LocalDate.of(2024, 1, 10); }
            public LocalDate getRequiredDate() { return LocalDate.of(2024, 1, 20); }
            public LocalDate getShippedDate() { return null; }
            public String getStatus() { return status; }
            public CustomerInfo getCustomer() { return null; }
        };
    }

    private PaymentListView buildPayment(String checkNumber, Integer customerNumber,
                                         BigDecimal amount) {
        return new PaymentListView() {
            public PaymentIdInfo getId() {
                return new PaymentIdInfo() {
                    public String getCheckNumber() { return checkNumber; }
                    public Integer getCustomerNumber() { return customerNumber; }
                };
            }
            public Integer getOrderNumber() { return 10100; }
            public LocalDate getPaymentDate() { return LocalDate.of(2024, 1, 15); }
            public BigDecimal getAmount() { return amount; }
        };
    }



    @Test
    void getStats_shouldReturn200AndFields() throws Exception {
        when(customerService.getTotalCustomers()).thenReturn(122L);
        when(customerService.getTotalCountries()).thenReturn(28L);
        when(customerService.getAvgCreditLimit()).thenReturn(new BigDecimal("67659.05"));

        mockMvc.perform(get("/api/v1/customers/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCustomers").value(122))
                .andExpect(jsonPath("$.totalCountries").value(28))
                .andExpect(jsonPath("$.avgCreditLimit").exists());
    }



    @Test
    void searchByName_shouldReturn200AndPage() throws Exception {
        CustomerListView c = buildCustomer(103, "Atelier graphique",
                "Nantes", "France", new BigDecimal("21000.00"));

        when(customerRepo.findByCustomerNameContainingIgnoreCase(eq("atelier"), any()))
                .thenReturn(new PageImpl<>(List.of(c), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/v1/customers/search").param("name", "atelier"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].customerNumber").value(103))
                .andExpect(jsonPath("$.content[0].city").value("Nantes"))
                .andExpect(jsonPath("$.content[0].country").value("France"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchByName_noMatch_shouldReturnEmptyPage() throws Exception {
        when(customerRepo.findByCustomerNameContainingIgnoreCase(eq("xyzxyz"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/customers/search").param("name", "xyzxyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }



    @Test
    void searchByPhone_shouldReturn200AndPage() throws Exception {
        CustomerListView c = buildCustomer(103, "Atelier graphique",
                "Nantes", "France", new BigDecimal("21000.00"));

        when(customerRepo.findByPhoneContaining(eq("40.32"), any()))
                .thenReturn(new PageImpl<>(List.of(c), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/v1/customers/search/phone").param("phone", "40.32"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].customerNumber").value(103))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchByPhone_noMatch_shouldReturnEmptyPage() throws Exception {
        when(customerRepo.findByPhoneContaining(eq("0000000000"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/customers/search/phone").param("phone", "0000000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }



    @Test
    void filterByCountry_shouldReturn200AndPage() throws Exception {
        CustomerListView c = buildCustomer(103, "Atelier graphique",
                "Nantes", "France", new BigDecimal("21000.00"));

        when(customerRepo.findByCountry(eq("France"), any()))
                .thenReturn(new PageImpl<>(List.of(c), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/v1/customers/filter/country").param("country", "France"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].country").value("France"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void filterByCountry_noMatch_shouldReturnEmptyPage() throws Exception {
        when(customerRepo.findByCountry(eq("Antarctica"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/customers/filter/country").param("country", "Antarctica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }



    @Test
    void filterByCreditLimit_shouldReturn200AndPage() throws Exception {
        CustomerListView c = buildCustomer(103, "Atelier graphique",
                "Nantes", "France", new BigDecimal("21000.00"));

        when(customerRepo.findByCreditLimitBetween(
                eq(new BigDecimal("5000")), eq(new BigDecimal("25000")), any()))
                .thenReturn(new PageImpl<>(List.of(c), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/v1/customers/filter/credit")
                        .param("min", "5000")
                        .param("max", "25000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].customerNumber").value(103))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void filterByCreditLimit_noMatch_shouldReturnEmptyPage() throws Exception {
        when(customerRepo.findByCreditLimitBetween(
                eq(new BigDecimal("99000")), eq(new BigDecimal("99999")), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/customers/filter/credit")
                        .param("min", "99000")
                        .param("max", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }



    @Test
    void getOrders_shouldReturn200AndList() throws Exception {
        OrderListView o = buildOrder(10100, "Shipped");

        when(orderRepo.findByCustomer_CustomerNumber(103))
                .thenReturn(List.of(o));

        mockMvc.perform(get("/api/v1/customers/103/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderNumber").value(10100))
                .andExpect(jsonPath("$[0].status").value("Shipped"));
    }

    @Test
    void getOrders_noOrders_shouldReturnEmptyList() throws Exception {
        when(orderRepo.findByCustomer_CustomerNumber(99999))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/customers/99999/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }



    @Test
    void getPayments_shouldReturn200AndList() throws Exception {
        PaymentListView p = buildPayment("HQ336336", 103, new BigDecimal("6066.78"));

        when(paymentRepo.findById_CustomerNumber(103))
                .thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/customers/103/payments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id.checkNumber").value("HQ336336"))
                .andExpect(jsonPath("$[0].amount").value(6066.78));
    }

    @Test
    void getPayments_noPayments_shouldReturnEmptyList() throws Exception {
        when(paymentRepo.findById_CustomerNumber(99999))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/customers/99999/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}