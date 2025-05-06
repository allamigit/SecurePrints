package com.secure.prints.controller;

import com.secure.prints.database.entity.CompanyEntity;
import com.secure.prints.service.CompanyService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
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
    public static CompanyEntity getCompanyDetails(@RequestParam(name = "companyId") int companyId) {
        return CompanyService.getCompanyDetails(companyId);
    }

    /**
     * Update Company Details
     * @param companyEntity companyEntity
     */
    @PutMapping(value = "update-company", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateCompanyDetails(@RequestBody CompanyEntity companyEntity) {
        companyService.updateCompanyDetails(companyEntity);
    }

}
