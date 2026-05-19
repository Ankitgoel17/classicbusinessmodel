package com.project.cmb.repo;

import com.project.cmb.entity.Office;
import com.project.cmb.projection.OfficeDetailView;
import com.project.cmb.projection.OfficeListView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "office")
public interface OfficeRepo extends JpaRepository<Office, String> {

    List<OfficeListView> findAllProjectedBy();

    Optional<OfficeDetailView> findOfficeDetailByOfficeCode(String officeCode);

    List<OfficeListView> findByCountryIn(List<String> countries);
}