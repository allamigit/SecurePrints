package com.secure.prints.database;

import com.secure.prints.database.entity.InvoiceEntity;
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
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    /**
     * Get Invoice Details
     * @param invoiceId invoiceId
     * @return InvoiceEntity
     */
    InvoiceEntity findByInvoiceId(@Param("invoiceId") long invoiceId);

    /**
     * GetInvoice Details by invoiceNumber
     * @param invoiceNumber invoiceNumber
     * @return InvoiceEntity
     */
    @Query(value = "SELECT i FROM InvoiceEntity i WHERE i.invoiceNumber = :invoiceNumber")
    InvoiceEntity findByInvoiceNumber(@Param("invoiceNumber") String invoiceNumber);

    /**
     * Reconcile Invoice
     * @param invoiceId invoiceId
     * @param invoiceReconcileDate invoiceReconcileDate
     */
    @Modifying
    @Query(value = "UPDATE InvoiceEntity i SET i.invoiceReconcileDate = :invoiceReconcileDate " +
            "WHERE i.invoiceId = :invoiceId")
    void reconcileInvoice(@Param("invoiceId") long invoiceId,
                          @Param("invoiceReconcileDate") LocalDate invoiceReconcileDate);

    /**
     * Get invoices list for all Invoice Information table data
     * @return List of invoices
     */
    @Query(value = "SELECT i FROM InvoiceEntity i ORDER BY i.invoiceDate DESC")
    List<InvoiceEntity> getAllInvoices();

    /**
     * Get invoices list for a specific date range
     * @return List of invoices
     */
    @Query(value = "SELECT i FROM InvoiceEntity i WHERE i.invoiceDate BETWEEN :startDate AND :endDate ORDER BY i.invoiceDate DESC")
    List<InvoiceEntity> getAllInvoicesForDateRange(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * Get invoices list for a specific date range for non-reconciled transactions
     * @return List of invoices
     */
    @Query(value = "SELECT i FROM InvoiceEntity i WHERE i.invoiceReconcileDate is null AND " +
            "i.invoiceDate BETWEEN :startDate AND :endDate ORDER BY i.invoiceDate DESC")
    List<InvoiceEntity> getNonReconciledInvoicesForDateRange(@Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    /**
     * Get total of invoice amount for a specific date range
     * @return Total of invoice amount
     */
    @Query(value = "SELECT SUM(i.invoiceAmount) FROM InvoiceEntity i WHERE i.invoiceDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalInvoiceAmount(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

}
