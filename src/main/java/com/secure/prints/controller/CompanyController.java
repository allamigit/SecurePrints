package com.secure.prints.controller;

import com.secure.prints.database.entity.CompanyEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.service.CompanyService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Constructor for CompanyController
     * @param companyService companyService
     */
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * Get Company Details
     * @param companyId companyId
     * @return CompanyEntity
     */
    @GetMapping(value = "company", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompanyEntity getCompanyDetails(@RequestParam(name = "companyId") int companyId) {
        return CompanyService.companyEntity;
    }

    /**
     * Update Company Details
     * @param companyDetails companyDetails
     */
    @PutMapping(value = "update-company", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus updateCompanyDetails(HttpServletResponse response,
                                          @RequestBody CompanyEntity companyDetails) {
        ApiStatus apiStatus = companyService.updateCompanyDetails(companyDetails);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

}
