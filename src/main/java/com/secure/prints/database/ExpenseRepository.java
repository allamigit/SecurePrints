package com.secure.prints.database;

import com.secure.prints.database.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
@Transactional
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    /**
     * Get next value of expense sequence
     * @return nextExpenseId
     */
    @Query(value = "SELECT nextval('exp_info_seq')")
    long getNextExpenseId();

    /**
     * Get Expense Details
     * @param expenseId expenseId
     * @return ExpenseEntity
     */
    ExpenseEntity findByExpenseId(@Param("expenseId") long expenseId);

    /**
     * Get Expense Details by expenseReferenceNumber
     * @param expenseReferenceNumber expenseReferenceNumber
     * @return ExpenseEntity
     */
    @Query(value = "SELECT e FROM ExpenseEntity e WHERE e.expenseReferenceNumber = :expenseReferenceNumber")
    ExpenseEntity findByExpenseReferenceNumber(@Param("expenseReferenceNumber") String expenseReferenceNumber);

    /**
     * Reconcile Expense
     * @param expenseId expenseId
     * @param expenseReconcileDate expenseReconcileDate
     */
    @Modifying
    @Query(value = "UPDATE ExpenseEntity e SET e.expenseReconcileDate = :expenseReconcileDate WHERE e.expenseId = :expenseId")
    void reconcileExpense(@Param("expenseId") long expenseId,
                          @Param("expenseReconcileDate") LocalDate expenseReconcileDate);

    /**
     * Get expenses list for all Expense Information table data
     * @return List of expenses
     */
    @Query(value = "SELECT e FROM ExpenseEntity e ORDER BY e.expenseReferenceDate DESC")
    List<ExpenseEntity> getAllExpenses();

    /**
     * Get expenses list for a specific date range
     * @return List of expenses
     */
    @Query(value = "SELECT e FROM ExpenseEntity e WHERE e.expenseReferenceDate BETWEEN :startDate AND :endDate ORDER BY e.expenseReferenceDate DESC")
    List<ExpenseEntity> getAllExpensesForDateRange(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * Get expenses list for a specific date range for non-reconciled transactions
     * @return List of expenses
     */
    @Query(value = "SELECT e FROM ExpenseEntity e WHERE e.expenseReconcileDate is null AND " +
            "e.expenseReferenceDate BETWEEN :startDate AND :endDate ORDER BY e.expenseReferenceDate DESC")
    List<ExpenseEntity> getNonReconciledExpensesForDateRange(@Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

}
