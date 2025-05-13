package com.secure.prints.database;

import com.secure.prints.database.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    /**
     * Get next value of expense sequence
     * @return nextExpenseId
     */
    @Query(value = "SELECT nextval('exp_info_seq')")
    long getNextExpenseId();

}
