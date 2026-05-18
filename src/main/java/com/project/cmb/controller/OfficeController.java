package com.project.cmb.controller;

import com.project.cmb.entity.Employee;
import com.project.cmb.entity.Office;
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

    // GET /api/v1/offices/stats
    // Total offices and total employees
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOffices", officeRepo.count());
        stats.put("totalEmployees", employeeRepo.count());
        return ResponseEntity.ok(stats);
    }

    // GET /api/v1/offices/filter/country?countries=USA&countries=France
    // Filter offices by one or more countries
    @GetMapping("/filter/country")
    public ResponseEntity<List<OfficeListView>> filterByCountry(
            @RequestParam List<String> countries) {
        return ResponseEntity.ok(officeRepo.findByCountryIn(countries));
    }

    // GET /api/v1/offices/{officeCode}/employees?page=0&size=10
    // Get all employees in a specific office
    @GetMapping("/{officeCode}/employees")
    public ResponseEntity<Page<EmployeeListView>> getEmployees(
            @PathVariable String officeCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                employeeRepo.findByOffice_OfficeCode(officeCode, pageable));
    }

    // PUT /api/v1/offices/{officeCode}/phone/{phone}
    // Update office phone number only
    @PutMapping("/{officeCode}/phone/{phone}")
    public ResponseEntity<Office> updatePhone(
            @PathVariable String officeCode,
            @PathVariable String phone) {
        Office office = officeRepo.findById(officeCode)
                .orElseThrow(() -> new RuntimeException(
                        "Office not found: " + officeCode));
        office.setPhone(phone);
        return ResponseEntity.ok(officeRepo.save(office));
    }
}