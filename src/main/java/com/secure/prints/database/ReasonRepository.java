package com.secure.prints.database;

import com.secure.prints.database.entity.ReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ReasonRepository extends JpaRepository<ReasonEntity, Long> {

    /**
     * Get all reasons from table by list type (BCI or FBI)
     * @return List of reasons
     */
    @Query(value = "SELECT r FROM ReasonEntity r WHERE r.reasonListType = :reasonListType ORDER BY r.reasonText")
    List<ReasonEntity> getAllReasonsByType(@Param("reasonListType") String reasonListType);

    /**
     * Remove all records from rsn_list table
     */
    @Modifying
    @Query(value = "TRUNCATE TABLE rsn_list", nativeQuery = true)
    void removeAllReasonData();

}
