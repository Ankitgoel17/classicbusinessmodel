package com.project.cmb.projection;

import org.springframework.data.rest.core.config.Projection;
import com.project.cmb.entity.Office;

@Projection(name = "officeList", types = { Office.class })
public interface OfficeListView {

    String getOfficeCode();
    String getCity();
    String getCountry();
    String getTerritory();
    String getPhone();

    // employeeCount handled in controller via employeeRepo.countByOffice_OfficeCode()
    // not a direct field on Office entity
}