package com.secure_prints.database;

import com.secure_prints.database.entity.ReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ReasonRepository extends JpaRepository<ReasonEntity, Long> {

    @Query(value = "SELECT r FROM ReasonEntity r ORDER BY r.serviceCode, r.reasonText")
    List<ReasonEntity> getAllReasons();

    @Query(value = "SELECT r FROM ReasonEntity r WHERE r.serviceCode = :serviceCode ORDER BY r.reasonText")
    List<ReasonEntity> getAllReasonsByServiceCode(@Param("serviceCode") String serviceCode);

}
