package com.project.cmb.projection;

import org.springframework.data.rest.core.config.Projection;
import com.project.cmb.entity.Office;

@Projection(name = "officeDetail", types = { Office.class })
public interface OfficeDetailView {

    String getOfficeCode();
    String getCity();
    String getCountry();
    String getTerritory();
    String getPhone();
    String getAddressLine1();
    String getAddressLine2();
    String getState();
    String getPostalCode();

    // employees list fetched separately via:
    // GET /api/v1/offices/{officeCode}/employees
}