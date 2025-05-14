package com.secure.prints.service;

import com.secure.prints.database.CompanyRepository;
import com.secure.prints.database.entity.CompanyEntity;
import com.secure.prints.model.ApiStatus;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private static CompanyRepository companyRepository = null;
    private static CompanyEntity companyEntity;

    /**
     * Constructor for CompanyService
     * @param companyRepository companyRepository
     */
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * Get Company Details
     * @param companyId companyId
     * @return CompanyEntity
     */
    public static CompanyEntity getCompanyDetails(int companyId) {
        companyEntity = companyRepository.findCompanyById(companyId);
        return companyEntity;
    }

    /**
     * Update Company Details
     * @param companyDetails companyDetails
     */
    public ApiStatus updateCompanyDetails(CompanyEntity companyDetails) {
        int responseCode = 409;
        String responseMessage;
        if(companyEntity.equals(companyDetails)) {
            responseMessage = "There is no change to update.";
        } else {
            try {
                companyRepository.save(companyDetails);
                getCompanyDetails(companyDetails.getCompanyId());
                responseCode = 200;
                responseMessage = "Company details updated successfully.";
            } catch (Exception e) {
                responseCode = 400;
                responseMessage = e.getCause().getMessage();
                if(responseMessage.contains("unique constraint")) {
                    responseMessage = "Duplicate company name.";
                }
            }
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();

    }

}
