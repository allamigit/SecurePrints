package com.secure.prints.database;

import com.secure.prints.database.entity.ExpenseEntity;
import com.secure.prints.model.ExpenseResultset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@Transactional
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

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
    @Query(value = "UPDATE ExpenseEntity e SET e.expenseReconcileDate = :expenseReconcileDate, e.expenseUpdate = :expenseUpdate " +
            "WHERE e.expenseId = :expenseId")
    void reconcileExpense(@Param("expenseId") long expenseId,
                          @Param("expenseReconcileDate") LocalDate expenseReconcileDate,
                          @Param("expenseUpdate") boolean expenseUpdate);

    /**
     * Adjust Fee
     * @param expenseReferenceNumber expenseReferenceNumber
     * @param expenseAmount expenseAmount
     */
    @Modifying
    @Query(value = "UPDATE ExpenseEntity e SET e.expenseAmount = :expenseAmount WHERE e.expenseReferenceNumber = :expenseReferenceNumber")
    void adjustFee(@Param("expenseReferenceNumber") String expenseReferenceNumber,
                   @Param("expenseAmount") BigDecimal expenseAmount);

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

    /**
     * Get total of bank fees amount CC Reader (Pending & Processed) for a specific date range
     * @return Total of bank fees amount
     */
    @Query(value = "SELECT SUM(e.expenseAmount) FROM ExpenseEntity e WHERE e.expenseReferenceNumber LIKE 'ApptID-%' AND e.expenseReferenceDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalBankFeesAmountAll(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * Get total of expense amount other than CC Reader (Pending & Processed) for a specific date range
     * @return Total of expense amount
     */
    @Query(value = "SELECT SUM(e.expenseAmount) FROM ExpenseEntity e WHERE e.expenseReferenceNumber NOT LIKE 'ApptID-%' AND e.expenseReferenceDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpenseAmountAll(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * Get total of bank fees amount CC Reader (Processed) for a specific date range
     * @return Total of bank fees amount
     */
    @Query(value = "SELECT SUM(e.expenseAmount) FROM ExpenseEntity e WHERE e.expenseReferenceNumber LIKE 'ApptID-%' AND e.expensePaymentStatusCode <> 201 AND e.expenseReferenceDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalBankFeesAmountProcessed(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * Get total of expense amount other than CC Reader (Processed) for a specific date range
     * @return Total of expense amount
     */
    @Query(value = "SELECT SUM(e.expenseAmount) FROM ExpenseEntity e WHERE e.expenseReferenceNumber NOT LIKE 'ApptID-%' AND e.expensePaymentStatusCode <> 201 AND e.expenseReferenceDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpenseAmountProcessed(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * Get list of total expense amount (Pending & Processed) for a specific date range
     * @return List of total expense amount
     */
    @Query(value = "SELECT new com.secure.prints.model.ExpenseResultset(e.expenseCategoryCode, e.expenseSubcategoryCode, SUM(e.expenseAmount)) FROM ExpenseEntity e " +
            "WHERE e.expensePaymentDate BETWEEN :startDate AND :endDate " +
            "GROUP BY e.expenseCategoryCode, e.expenseSubcategoryCode " +
            "ORDER BY e.expenseCategoryCode, e.expenseSubcategoryCode")
    List<ExpenseResultset> getExpenseTotalsAll(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * Get list of total expense amount (Processed) for a specific date range
     * @return List of total expense amount
     */
    @Query(value = "SELECT new com.secure.prints.model.ExpenseResultset(e.expenseCategoryCode, e.expenseSubcategoryCode, SUM(e.expenseAmount)) FROM ExpenseEntity e " +
            "WHERE e.expensePaymentStatusCode <> 201 AND e.expensePaymentDate BETWEEN :startDate AND :endDate " +
            "GROUP BY e.expenseCategoryCode, e.expenseSubcategoryCode " +
            "ORDER BY e.expenseCategoryCode, e.expenseSubcategoryCode")
    List<ExpenseResultset> getExpenseTotalsProcessed(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

}
