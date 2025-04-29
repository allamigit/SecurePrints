package com.secure.prints.database;

import com.secure.prints.database.entity.ReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ReasonRepository extends JpaRepository<ReasonEntity, Long> {

    /**
     * Get all reasons from table
     * @return List of all reasons
     */
    @Query(value = "SELECT r FROM ReasonEntity r ORDER BY r.reasonListType, r.reasonText")
    List<ReasonEntity> getAllReasons();

}
