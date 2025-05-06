package com.secure.prints.service;

import com.secure.prints.database.CompanyRepository;
import com.secure.prints.database.entity.CompanyEntity;
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
     * @param companyEntity companyEntity
     */
    public void updateCompanyDetails(CompanyEntity companyEntity) {
        companyRepository.save(companyEntity);
        getCompanyDetails(companyEntity.getCompanyId());
    }

}
