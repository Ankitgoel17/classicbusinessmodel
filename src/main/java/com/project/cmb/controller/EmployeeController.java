package com.project.cmb.controller;

import com.project.cmb.entity.Employee;
import com.project.cmb.entity.Office;
import com.project.cmb.exception.ResourceNotFoundException;
import com.project.cmb.projection.CustomerListView;
import com.project.cmb.projection.EmployeeDetailView;
import com.project.cmb.projection.EmployeeListView;
import com.project.cmb.repo.CustomerRepo;
import com.project.cmb.repo.EmployeeRepo;
import com.project.cmb.repo.OfficeRepo;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/employees")
@AllArgsConstructor
public class EmployeeController {

    private final EmployeeRepo employeeRepo;
    private final CustomerRepo customerRepo;
    private final OfficeRepo   officeRepo;

    @GetMapping("/{employeeNumber}")
    public ResponseEntity<EmployeeDetailView> getEmployee(@PathVariable Integer employeeNumber) {
        return employeeRepo.findDetailByEmployeeNumber(employeeNumber)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "employeeNumber", employeeNumber));
    }

    // ─── POST /add — create employee ─────────────────────────────
    @PostMapping("/add")
    public ResponseEntity<?> addEmployee(@RequestBody Map<String, Object> dto) {
        Employee e = new Employee();
        e.setEmployeeNumber(Integer.valueOf(dto.get("employeeNumber").toString()));
        e.setFirstName((String) dto.get("firstName"));
        e.setLastName((String)  dto.get("lastName"));
        e.setEmail((String)     dto.get("email"));
        e.setExtension((String) dto.getOrDefault("extension", "x000"));
        e.setJobTitle((String)  dto.get("jobTitle"));
        if (dto.get("officeCode") != null) {
            officeRepo.findById((String) dto.get("officeCode")).ifPresent(e::setOffice);
        }
        if (dto.get("reportsTo") != null && !dto.get("reportsTo").toString().isBlank()) {
            Integer mgrNo = Integer.valueOf(dto.get("reportsTo").toString());
            employeeRepo.findById(mgrNo).ifPresent(e::setReportsTo);
        }
        Employee saved = employeeRepo.save(e);
        return ResponseEntity.status(201).body(
                Map.of("employeeNumber", saved.getEmployeeNumber(), "message", "Employee created"));
    }

    // ─── PUT /update/{no} — update employee ──────────────────────
    @PutMapping("/update/{employeeNumber}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable Integer employeeNumber,
            @RequestBody Map<String, Object> dto) {
        Employee e = employeeRepo.findById(employeeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "employeeNumber", employeeNumber));
        if (dto.get("firstName") != null)  e.setFirstName((String) dto.get("firstName"));
        if (dto.get("lastName")  != null)  e.setLastName((String)  dto.get("lastName"));
        if (dto.get("email")     != null)  e.setEmail((String)     dto.get("email"));
        if (dto.get("extension") != null)  e.setExtension((String) dto.get("extension"));
        if (dto.get("jobTitle")  != null)  e.setJobTitle((String)  dto.get("jobTitle"));
        if (dto.get("officeCode") != null) {
            officeRepo.findById((String) dto.get("officeCode")).ifPresent(e::setOffice);
        }
        if (dto.get("reportsTo") != null && !dto.get("reportsTo").toString().isBlank()) {
            Integer mgrNo = Integer.valueOf(dto.get("reportsTo").toString());
            employeeRepo.findById(mgrNo).ifPresent(e::setReportsTo);
        }
        employeeRepo.save(e);
        return ResponseEntity.ok(Map.of("message", "Employee updated"));
    }

    // ─── DELETE /delete/{no} — delete employee ───────────────────
    @DeleteMapping("/delete/{employeeNumber}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Integer employeeNumber) {
        if (!employeeRepo.existsById(employeeNumber))
            return ResponseEntity.notFound().build();
        employeeRepo.deleteById(employeeNumber);
        return ResponseEntity.noContent().build();
    }

    // ─── GET /search ─────────────────────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeListView>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                employeeRepo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        name, name, pageable));
    }

    // ─── GET /search/city ────────────────────────────────────────
    @GetMapping("/search/city")
    public ResponseEntity<Page<EmployeeListView>> searchByCity(
            @RequestParam String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                employeeRepo.findByOffice_CityContainingIgnoreCase(city, pageable));
    }

    // ─── GET /office/{officeCode} ────────────────────────────────
    @GetMapping("/office/{officeCode}")
    public ResponseEntity<Page<EmployeeListView>> getByOffice(
            @PathVariable String officeCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                employeeRepo.findByOffice_OfficeCode(officeCode, pageable));
    }

    // ─── GET /{no}/reportees ─────────────────────────────────────
    @GetMapping("/{employeeNumber}/reportees")
    public ResponseEntity<List<EmployeeListView>> getReportees(
            @PathVariable Integer employeeNumber) {
        return ResponseEntity.ok(
                employeeRepo.findByReportsTo_EmployeeNumber(employeeNumber));
    }

    // ─── GET /{no}/customers ─────────────────────────────────────
    @GetMapping("/{employeeNumber}/customers")
    public ResponseEntity<Page<CustomerListView>> getCustomers(
            @PathVariable Integer employeeNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                customerRepo.findBySalesRepEmployee_EmployeeNumber(
                        employeeNumber, pageable));
    }

    // ─── GET /top-level ──────────────────────────────────────────
    @GetMapping("/top-level")
    public ResponseEntity<List<EmployeeListView>> getTopLevelEmployees() {
        return ResponseEntity.ok(employeeRepo.findByReportsToIsNull());
    }
}