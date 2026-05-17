package com.project.cmb.repository;

import com.project.cmb.entity.Office;
import com.project.cmb.projection.OfficeListView;
import com.project.cmb.repo.OfficeRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OfficeRepoTest {

    @Autowired private OfficeRepo officeRepo;

    @BeforeEach
    void setup() {
        officeRepo.save(Office.builder().officeCode("T01").city("Test City A")
                .phone("+1 111 111 1111").addressLine1("1 Test Street")
                .country("USA").postalCode("10001").territory("NA").build());
        officeRepo.save(Office.builder().officeCode("T02").city("Test City B")
                .phone("+33 1 11 11 11 11").addressLine1("2 Test Avenue")
                .country("France").postalCode("75001").territory("EMEA").build());
        officeRepo.save(Office.builder().officeCode("T03").city("Test City C")
                .phone("+44 111 111 1111").addressLine1("3 Test Road")
                .country("UK").postalCode("EC1A 1BB").territory("EMEA").build());
    }

    // --- findAll ---

    @Test
    void repo_findAll_shouldReturnOffices() {
        assertThat(officeRepo.findAll()).isNotEmpty();
    }

    // --- findById ---

    @Test
    void repo_findById_shouldReturnOffice() {
        Optional<Office> result = officeRepo.findById("T01");
        assertThat(result).isPresent();
        assertThat(result.get().getCity()).isEqualTo("Test City A");
    }

    @Test
    void repo_findById_whenNotExists_shouldReturnEmpty() {
        assertThat(officeRepo.findById("ZZZ")).isEmpty();
    }

    // --- save ---

    @Test
    void repo_save_shouldPersistNewOffice() {
        officeRepo.save(Office.builder().officeCode("T04").city("Test City D")
                .phone("+81 11 1111 1111").addressLine1("4 Test Blvd")
                .country("Japan").postalCode("100-0001").territory("Japan").build());
        assertThat(officeRepo.findById("T04")).isPresent();
    }

    @Test
    void repo_save_shouldIncreaseCount() {
        long countBefore = officeRepo.count();
        officeRepo.save(Office.builder().officeCode("T05").city("Test City E")
                .phone("+65 1111 1111").addressLine1("5 Test Lane")
                .country("Singapore").postalCode("018989").territory("APAC").build());
        assertThat(officeRepo.count()).isEqualTo(countBefore + 1);
    }

    // --- update ---

    @Test
    void repo_update_phone_shouldModifyPhone() {
        Office o = officeRepo.findById("T01").orElseThrow();
        o.setPhone("+1 999 999 9999");
        officeRepo.save(o);
        assertThat(officeRepo.findById("T01").orElseThrow().getPhone())
                .isEqualTo("+1 999 999 9999");
    }

    @Test
    void repo_update_shouldNotChangeOtherFields() {
        Office o = officeRepo.findById("T01").orElseThrow();
        o.setPhone("+1 888 888 8888");
        officeRepo.save(o);
        Office updated = officeRepo.findById("T01").orElseThrow();
        assertThat(updated.getCity()).isEqualTo("Test City A");
        assertThat(updated.getCountry()).isEqualTo("USA");
    }

    // --- delete ---

    @Test
    void repo_deleteById_shouldRemoveOffice() {
        officeRepo.deleteById("T03");
        assertThat(officeRepo.findById("T03")).isEmpty();
    }

    @Test
    void repo_deleteById_shouldDecreaseCount() {
        long countBefore = officeRepo.count();
        officeRepo.deleteById("T03");
        assertThat(officeRepo.count()).isEqualTo(countBefore - 1);
    }

    // --- findByCountryIn ---

    @Test
    void repo_findByCountryIn_shouldReturnMatchingOffices() {
        List<OfficeListView> result = officeRepo.findByCountryIn(List.of("USA", "France"));
        assertThat(result.stream()
                .anyMatch(o -> o.getOfficeCode().equals("T01"))).isTrue();
        assertThat(result.stream()
                .anyMatch(o -> o.getOfficeCode().equals("T02"))).isTrue();
    }

    @Test
    void repo_findByCountryIn_singleCountry_shouldWork() {
        List<OfficeListView> result = officeRepo.findByCountryIn(List.of("UK"));
        assertThat(result.stream()
                .anyMatch(o -> o.getOfficeCode().equals("T03"))).isTrue();
    }

    @Test
    void repo_findByCountryIn_noMatch_shouldReturnEmpty() {
        List<OfficeListView> result = officeRepo.findByCountryIn(List.of("Antarctica"));
        assertThat(result).isEmpty();
    }

    @Test
    void repo_findByCountryIn_shouldNotReturnOtherCountries() {
        List<OfficeListView> result = officeRepo.findByCountryIn(List.of("USA"));
        assertThat(result.stream()
                .allMatch(o -> o.getCountry().equals("USA"))).isTrue();
    }
}