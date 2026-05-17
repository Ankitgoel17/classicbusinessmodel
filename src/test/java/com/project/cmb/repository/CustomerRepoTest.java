package com.project.cmb.repository;

import com.project.cmb.entity.Customer;
import com.project.cmb.projection.CustomerListView;
import com.project.cmb.repo.CustomerRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepoTest {

    @Autowired private CustomerRepo customerRepo;

    @BeforeEach
    void setup() {
        Customer c1 = new Customer();
        c1.setCustomerNumber(9101); c1.setCustomerName("Test Alpha Corp");
        c1.setContactFirstName("John"); c1.setContactLastName("Doe");
        c1.setPhone("1111111111"); c1.setAddressLine1("123 Main St");
        c1.setCity("New York"); c1.setCountry("USA");
        c1.setCreditLimit(new BigDecimal("10000.00"));
        customerRepo.save(c1);

        Customer c2 = new Customer();
        c2.setCustomerNumber(9102); c2.setCustomerName("Test Beta Ltd");
        c2.setContactFirstName("Jane"); c2.setContactLastName("Smith");
        c2.setPhone("2222222222"); c2.setAddressLine1("456 Oak Ave");
        c2.setCity("Paris"); c2.setCountry("France");
        c2.setCreditLimit(new BigDecimal("25000.00"));
        customerRepo.save(c2);

        Customer c3 = new Customer();
        c3.setCustomerNumber(9103); c3.setCustomerName("Test Gamma Inc");
        c3.setContactFirstName("Bob"); c3.setContactLastName("Jones");
        c3.setPhone("3333333333"); c3.setAddressLine1("789 Pine Rd");
        c3.setCity("London"); c3.setCountry("UK");
        c3.setCreditLimit(new BigDecimal("5000.00"));
        customerRepo.save(c3);
    }


    @Test
    void repo_findAll_shouldReturnCustomers() {
        Page<Customer> result = customerRepo.findAll(PageRequest.of(0, 5));
        assertThat(result.getTotalElements()).isGreaterThan(0);
    }


    @Test
    void repo_findById_shouldReturnCustomer() {
        Optional<Customer> result = customerRepo.findById(9101);
        assertThat(result).isPresent();
        assertThat(result.get().getCustomerName()).isEqualTo("Test Alpha Corp");
    }

    @Test
    void repo_findById_whenNotExists_shouldReturnEmpty() {
        assertThat(customerRepo.findById(99999)).isEmpty();
    }

    @Test
    void repo_save_shouldPersistNewCustomer() {
        Customer c = new Customer();
        c.setCustomerNumber(9104); c.setCustomerName("Test Delta Co");
        c.setContactFirstName("Alice"); c.setContactLastName("Brown");
        c.setPhone("4444444444"); c.setAddressLine1("321 Elm St");
        c.setCity("Berlin"); c.setCountry("Germany");
        c.setCreditLimit(new BigDecimal("15000.00"));
        customerRepo.save(c);
        assertThat(customerRepo.findById(9104)).isPresent();
    }

    @Test
    void repo_save_shouldIncreaseCount() {
        long countBefore = customerRepo.count();
        Customer c = new Customer();
        c.setCustomerNumber(9105); c.setCustomerName("Test Epsilon SA");
        c.setContactFirstName("Eve"); c.setContactLastName("White");
        c.setPhone("5555555555"); c.setAddressLine1("654 Birch Ln");
        c.setCity("Madrid"); c.setCountry("Spain");
        c.setCreditLimit(new BigDecimal("8000.00"));
        customerRepo.save(c);
        assertThat(customerRepo.count()).isEqualTo(countBefore + 1);
    }

    @Test
    void repo_update_shouldModifyCustomer() {
        Customer c = customerRepo.findById(9101).orElseThrow();
        c.setPhone("9999999999");
        customerRepo.save(c);
        assertThat(customerRepo.findById(9101).orElseThrow().getPhone())
                .isEqualTo("9999999999");
    }

    @Test
    void repo_update_shouldNotChangeOtherFields() {
        Customer c = customerRepo.findById(9101).orElseThrow();
        c.setCity("Chicago");
        customerRepo.save(c);
        Customer updated = customerRepo.findById(9101).orElseThrow();
        assertThat(updated.getCustomerName()).isEqualTo("Test Alpha Corp");
        assertThat(updated.getCountry()).isEqualTo("USA");
    }

    @Test
    void repo_deleteById_shouldRemoveCustomer() {
        customerRepo.deleteById(9103);
        assertThat(customerRepo.findById(9103)).isEmpty();
    }

    @Test
    void repo_deleteById_shouldDecreaseCount() {
        long countBefore = customerRepo.count();
        customerRepo.deleteById(9103);
        assertThat(customerRepo.count()).isEqualTo(countBefore - 1);
    }

    @Test
    void repo_findByName_shouldReturnMatch() {
        Page<CustomerListView> result = customerRepo
                .findByCustomerNameContainingIgnoreCase("alpha", PageRequest.of(0, 5));
        assertThat(result.getContent().stream()
                .anyMatch(c -> c.getCustomerNumber().equals(9101))).isTrue();
    }

    @Test
    void repo_findByName_caseInsensitive_shouldWork() {
        Page<CustomerListView> result = customerRepo
                .findByCustomerNameContainingIgnoreCase("BETA", PageRequest.of(0, 5));
        assertThat(result.getContent().stream()
                .anyMatch(c -> c.getCustomerNumber().equals(9102))).isTrue();
    }

    @Test
    void repo_findByName_noMatch_shouldReturnEmpty() {
        Page<CustomerListView> result = customerRepo
                .findByCustomerNameContainingIgnoreCase("xyzxyzxyz", PageRequest.of(0, 5));
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void repo_findByPhone_shouldReturnMatch() {
        Page<CustomerListView> result = customerRepo
                .findByPhoneContaining("2222", PageRequest.of(0, 5));
        assertThat(result.getContent().stream()
                .anyMatch(c -> c.getCustomerNumber().equals(9102))).isTrue();
    }

    @Test
    void repo_findByPhone_noMatch_shouldReturnEmpty() {
        Page<CustomerListView> result = customerRepo
                .findByPhoneContaining("0000000000", PageRequest.of(0, 5));
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void repo_findByCountry_shouldReturnMatch() {
        Page<CustomerListView> result = customerRepo
                .findByCountry("France", PageRequest.of(0, 100));
        assertThat(result.getContent().stream()
                .anyMatch(c -> c.getCustomerNumber().equals(9102))).isTrue();
    }

    @Test
    void repo_findByCountry_wrongCountry_shouldReturnEmpty() {
        Page<CustomerListView> result = customerRepo
                .findByCountry("Antarctica", PageRequest.of(0, 5));
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void repo_findByCreditLimitBetween_shouldReturnMatchingCustomers() {
        Page<CustomerListView> result = customerRepo
                .findByCreditLimitBetween(
                        new BigDecimal("8000.00"),
                        new BigDecimal("30000.00"),
                        PageRequest.of(0, 10));
        assertThat(result.getContent().stream()
                .anyMatch(c -> c.getCustomerNumber().equals(9101))).isTrue();
        assertThat(result.getContent().stream()
                .anyMatch(c -> c.getCustomerNumber().equals(9102))).isTrue();
        assertThat(result.getContent().stream()
                .noneMatch(c -> c.getCustomerNumber().equals(9103))).isTrue();
    }

    @Test
    void repo_findByCreditLimitBetween_exactBoundary_shouldInclude() {
        Page<CustomerListView> result = customerRepo
                .findByCreditLimitBetween(
                        new BigDecimal("10000.00"),
                        new BigDecimal("10000.00"),
                        PageRequest.of(0, 10));
        assertThat(result.getContent().stream()
                .anyMatch(c -> c.getCustomerNumber().equals(9101))).isTrue();
    }

    @Test
    void repo_findByCreditLimitBetween_noMatch_shouldReturnEmpty() {
        Page<CustomerListView> result = customerRepo
                .findByCreditLimitBetween(
                        new BigDecimal("99000.00"),
                        new BigDecimal("99999.00"),
                        PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void repo_findBySalesRep_shouldReturnEmpty_whenNoCustomersAssigned() {
        Page<CustomerListView> result = customerRepo
                .findBySalesRepEmployee_EmployeeNumber(99999, PageRequest.of(0, 5));
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void repo_existsByCustomerName_shouldReturnTrue_whenExists() {
        assertThat(customerRepo.existsByCustomerName("Test Alpha Corp")).isTrue();
    }

    @Test
    void repo_existsByCustomerName_shouldReturnFalse_whenNotExists() {
        assertThat(customerRepo.existsByCustomerName("Nonexistent Corp")).isFalse();
    }

    @Test
    void repo_existsByPhone_shouldReturnTrue_whenExists() {
        assertThat(customerRepo.existsByPhone("1111111111")).isTrue();
    }

    @Test
    void repo_existsByPhone_shouldReturnFalse_whenNotExists() {
        assertThat(customerRepo.existsByPhone("0000000000")).isFalse();
    }

    @Test
    void repo_existsByPhone_afterDelete_shouldReturnFalse() {
        customerRepo.deleteById(9101);
        assertThat(customerRepo.existsByPhone("1111111111")).isFalse();
    }
}