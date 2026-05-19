package com.project.cmb.repo;

import com.project.cmb.entity.Employee;
import com.project.cmb.projection.EmployeeDetailView;
import com.project.cmb.projection.EmployeeListView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "employees")
public interface EmployeeRepo extends JpaRepository<Employee, Integer> {

    Page<EmployeeListView> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    Page<EmployeeListView> findByOffice_OfficeCode(String officeCode, Pageable pageable);

    Page<EmployeeListView> findByOffice_CityContainingIgnoreCase(String city, Pageable pageable);

    List<EmployeeListView> findByReportsTo_EmployeeNumber(Integer employeeNumber);

    List<EmployeeListView> findByReportsToIsNull();

    long countByOffice_OfficeCode(String officeCode);

    Optional<EmployeeDetailView> findDetailByEmployeeNumber(Integer employeeNumber);
}

