package com.project.cmb.controller;

import com.project.cmb.entity.Office;
import com.project.cmb.projection.EmployeeListView;
import com.project.cmb.projection.OfficeListView;
import com.project.cmb.repo.EmployeeRepo;
import com.project.cmb.repo.OfficeRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OfficeController.class)
class OfficeControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean OfficeRepo officeRepo;
    @MockBean EmployeeRepo employeeRepo;

    private OfficeListView buildOfficeView(String code, String city,
                                           String country, String territory, String phone) {
        return new OfficeListView() {
            public String getOfficeCode() { return code; }
            public String getCity() { return city; }
            public String getCountry() { return country; }
            public String getTerritory() { return territory; }
            public String getPhone() { return phone; }
        };
    }

    private EmployeeListView buildEmployeeView(Integer number,
                                               String firstName, String lastName, String jobTitle) {
        return new EmployeeListView() {
            public Integer getEmployeeNumber() { return number; }
            public String getFirstName() { return firstName; }
            public String getLastName() { return lastName; }
            public String getJobTitle() { return jobTitle; }
            public OfficeInfo getOffice() { return null; }
            public ManagerInfo getReportsTo() { return null; }
        };
    }

    private Office buildOffice(String code, String city,
                               String country, String phone) {
        Office o = new Office();
        o.setOfficeCode(code); o.setCity(city);
        o.setCountry(country); o.setPhone(phone);
        o.setAddressLine1("100 Market Street");
        o.setTerritory("NA"); o.setPostalCode("94080");
        return o;
    }

    @Test
    void getStats_shouldReturn200AndFields() throws Exception {
        when(officeRepo.count()).thenReturn(7L);
        when(employeeRepo.count()).thenReturn(23L);

        mockMvc.perform(get("/api/v1/offices/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalOffices").value(7))
                .andExpect(jsonPath("$.totalEmployees").value(23));
    }

    @Test
    void filterByCountry_shouldReturn200AndList() throws Exception {
        OfficeListView o1 = buildOfficeView("1", "San Francisco", "USA", "NA",
                "+1 650 219 4782");
        OfficeListView o2 = buildOfficeView("4", "Paris", "France", "EMEA",
                "+33 14 723 4404");

        when(officeRepo.findByCountryIn(List.of("USA", "France")))
                .thenReturn(List.of(o1, o2));

        mockMvc.perform(get("/api/v1/offices/filter/country")
                        .param("countries", "USA")
                        .param("countries", "France"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].officeCode").value("1"))
                .andExpect(jsonPath("$[0].country").value("USA"))
                .andExpect(jsonPath("$[1].country").value("France"));
    }

    @Test
    void filterByCountry_noMatch_shouldReturnEmptyList() throws Exception {
        when(officeRepo.findByCountryIn(List.of("Antarctica")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/offices/filter/country")
                        .param("countries", "Antarctica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getEmployees_shouldReturn200AndPage() throws Exception {
        EmployeeListView emp = buildEmployeeView(1002, "Diane", "Murphy", "President");

        when(employeeRepo.findByOffice_OfficeCode(eq("1"), any()))
                .thenReturn(new PageImpl<>(List.of(emp), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/offices/1/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].employeeNumber").value(1002))
                .andExpect(jsonPath("$.content[0].firstName").value("Diane"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getEmployees_invalidOffice_shouldReturnEmptyPage() throws Exception {
        when(employeeRepo.findByOffice_OfficeCode(eq("999"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/offices/999/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void updatePhone_shouldReturn200AndUpdatedOffice() throws Exception {
        Office office = buildOffice("1", "San Francisco", "USA",
                "+1 650 219 4782");
        Office updated = buildOffice("1", "San Francisco", "USA",
                "+1 650 999 9999");

        when(officeRepo.findById("1")).thenReturn(Optional.of(office));
        when(officeRepo.save(any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/offices/1/phone/+1 650 999 9999"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.officeCode").value("1"))
                .andExpect(jsonPath("$.phone").value("+1 650 999 9999"));
    }

    @Test
    void updatePhone_officeNotFound_shouldReturn404() throws Exception {
        when(officeRepo.findById("999")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/offices/999/phone/+1 999 999 9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}