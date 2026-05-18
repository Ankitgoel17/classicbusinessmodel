package com.project.cmb.controller;

import com.project.cmb.entity.Office;
import com.project.cmb.exception.ResourceNotFoundException;
import com.project.cmb.projection.EmployeeListView;
import com.project.cmb.projection.OfficeListView;
import com.project.cmb.repo.EmployeeRepo;
import com.project.cmb.repo.OfficeRepo;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/offices")
@AllArgsConstructor
public class OfficeController {

    private final OfficeRepo officeRepo;
    private final EmployeeRepo employeeRepo;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOffices", officeRepo.count());
        stats.put("totalEmployees", employeeRepo.count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/filter/country")
    public ResponseEntity<List<OfficeListView>> filterByCountry(
            @RequestParam List<String> countries) {
        return ResponseEntity.ok(officeRepo.findByCountryIn(countries));
    }

    @GetMapping("/{officeCode}/employees")
    public ResponseEntity<Page<EmployeeListView>> getEmployees(
            @PathVariable String officeCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                employeeRepo.findByOffice_OfficeCode(officeCode, pageable));
    }

    @PutMapping("/{officeCode}/phone/{phone}")
    public ResponseEntity<Office> updatePhone(
            @PathVariable String officeCode,
            @PathVariable String phone) {
        Office office = officeRepo.findById(officeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Office", "officeCode", officeCode));
        office.setPhone(phone);
        return ResponseEntity.ok(officeRepo.save(office));
    }
}