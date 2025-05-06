package com.secure.prints.database;

import com.secure.prints.database.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface CompanyRepository extends JpaRepository<CompanyEntity, Integer> {

    /**
     * Get Company details
     * @param companyId companyId
     * @return CompanyEntity
     */
    @Query(value = "SELECT c FROM CompanyEntity c WHERE c.companyId = :companyId")
    CompanyEntity findCompanyById(@Param("companyId") int companyId);

}
