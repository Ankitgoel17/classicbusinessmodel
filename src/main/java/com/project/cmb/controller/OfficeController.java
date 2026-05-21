package com.project.cmb.controller;

import com.project.cmb.entity.Office;
import com.project.cmb.exception.DuplicateResourceException;
import com.project.cmb.exception.ResourceNotFoundException;
import com.project.cmb.projection.EmployeeListView;
import com.project.cmb.projection.OfficeDetailView;
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

    private final OfficeRepo   officeRepo;
    private final EmployeeRepo employeeRepo;

    // ── GET ALL ───────────────────────────────────────────────────────
    @GetMapping("/all")
    public ResponseEntity<List<OfficeListView>> getAllOffices() {
        return ResponseEntity.ok(officeRepo.findAllProjectedBy());
    }

    // ── GET ONE ───────────────────────────────────────────────────────
    @GetMapping("/{officeCode}")
    public ResponseEntity<OfficeDetailView> getOffice(@PathVariable String officeCode) {
        return officeRepo.findOfficeDetailByOfficeCode(officeCode)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Office", "officeCode", officeCode));
    }

    // ── STATS ─────────────────────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOffices",   officeRepo.count());
        stats.put("totalEmployees", employeeRepo.count());
        return ResponseEntity.ok(stats);
    }

    // ── FILTER BY COUNTRY ─────────────────────────────────────────────
    @GetMapping("/filter/country")
    public ResponseEntity<List<OfficeListView>> filterByCountry(
            @RequestParam List<String> countries) {
        return ResponseEntity.ok(officeRepo.findByCountryIn(countries));
    }

    // ── GET EMPLOYEES ─────────────────────────────────────────────────
    @GetMapping("/{officeCode}/employees")
    public ResponseEntity<Page<EmployeeListView>> getEmployees(
            @PathVariable String officeCode,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(employeeRepo.findByOffice_OfficeCode(officeCode, pageable));
    }

    // ── ADD ───────────────────────────────────────────────────────────
    @PostMapping("/add")
    public ResponseEntity<?> addOffice(@RequestBody Map<String, Object> dto) {
        String officeCode = (String) dto.get("officeCode");
        if (officeCode == null || officeCode.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "officeCode is required"));
        if (officeRepo.existsById(officeCode))
            throw new DuplicateResourceException("Office", "officeCode", officeCode);

        Office o = Office.builder()
                .officeCode(officeCode)
                .city((String)         dto.get("city"))
                .country((String)      dto.get("country"))
                .phone((String)        dto.get("phone"))
                .postalCode((String)   dto.get("postalCode"))
                .territory((String)    dto.get("territory"))
                .addressLine1((String) dto.get("addressLine1"))
                .addressLine2((String) dto.getOrDefault("addressLine2", null))
                .state((String)        dto.getOrDefault("state", null))
                .build();

        officeRepo.save(o);
        return ResponseEntity.status(201).body(
                Map.of("officeCode", o.getOfficeCode(), "message", "Office created"));
    }

    // ── UPDATE PHONE ──────────────────────────────────────────────────
    @PutMapping("/{officeCode}/phone")
    public ResponseEntity<Void> updatePhone(
            @PathVariable String officeCode,
            @RequestParam String phone) {
        Office office = officeRepo.findById(officeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Office", "officeCode", officeCode));
        office.setPhone(phone);
        officeRepo.save(office);
        return ResponseEntity.noContent().build();
    }
}