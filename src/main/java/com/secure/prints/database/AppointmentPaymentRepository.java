package com.secure.prints.database;

import com.secure.prints.database.entity.AppointmentPaymentEntity;
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
public interface AppointmentPaymentRepository extends JpaRepository<AppointmentPaymentEntity, String> {

    /**
     * Get appointment payment details by appointment ID
     * @param appointmentId appointmentId
     * @return AppointmentPaymentEntity
     */
    AppointmentPaymentEntity findPaymentByAppointmentId(String appointmentId);

    /**
     * Update appointment reconcile date
     * @param appointmentId appointmentId
     */
    @Modifying
    @Query(value = "UPDATE AppointmentPaymentEntity a SET a.paymentReconcileDate = :paymentReconcileDate, a.paymentUpdate = :paymentUpdate " +
            "WHERE a.appointmentId = :appointmentId")
    void reconcilePayment(@Param("appointmentId") String appointmentId,
                          @Param("paymentReconcileDate") LocalDate paymentReconcileDate,
                          @Param("paymentUpdate") boolean paymentUpdate);

    /**
     * Update payment status and date
     * @param appointmentId appointmentId
     * @param paymentStatusCode paymentStatusCode
     * @param paymentDate paymentDate
     */
    @Modifying
    @Query(value = "UPDATE AppointmentPaymentEntity a SET a.paymentStatusCode = :paymentStatusCode, a.paymentDate = :paymentDate " +
            "WHERE a.appointmentId = :appointmentId")
    void updatePaymentStatusAndDate(@Param("appointmentId") String appointmentId,
                                    @Param("paymentStatusCode") int paymentStatusCode,
                                    @Param("paymentDate") LocalDate paymentDate);

    /**
     * Update payment status and method
     * @param appointmentId appointmentId
     * @param paymentStatusCode paymentStatusCode
     * @param paymentMethodCode paymentMethodCode
     * @param transactionFees transactionFees
     */
    @Modifying
    @Query(value = "UPDATE AppointmentPaymentEntity a SET a.paymentStatusCode = :paymentStatusCode, a.paymentMethodCode = :paymentMethodCode, " +
            "a.serviceAmount = a.serviceAmount - :transactionFees WHERE a.appointmentId = :appointmentId")
    void updatePaymentStatusAndMethod(@Param("appointmentId") String appointmentId,
                                      @Param("paymentStatusCode") int paymentStatusCode,
                                      @Param("paymentMethodCode") int paymentMethodCode,
                                      @Param("transactionFees") BigDecimal transactionFees);

    /**
     * Update service amount, payment method and comment
     * @param appointmentId appointmentId
     * @param serviceAmount serviceAmount
     * @param paymentMethodCode paymentMethodCode
     * @param paymentComment paymentComment
     */
    @Modifying
    @Query(value = "UPDATE AppointmentPaymentEntity a SET a.serviceAmount = :serviceAmount, a.paymentMethodCode = :paymentMethodCode, " + "" +
            "a.paymentComment = :paymentComment WHERE a.appointmentId = :appointmentId")
    void updatePaymentDetails(@Param("appointmentId") String appointmentId,
                              @Param("serviceAmount") BigDecimal serviceAmount,
                              @Param("paymentMethodCode") int paymentMethodCode,
                              @Param("paymentComment") String paymentComment);

    /**
     * Adjust service amount
     * @param appointmentId appointmentId
     * @param differenceAmount differenceAmount
     */
    @Modifying
    @Query(value = "UPDATE AppointmentPaymentEntity a SET a.serviceAmount = a.serviceAmount + :differenceAmount " +
            "WHERE a.appointmentId = :appointmentId")
    void adjustServiceAmount(@Param("appointmentId") String appointmentId,
                             @Param("differenceAmount") BigDecimal differenceAmount);

    /**
     * Cleanup Cancelled payments from Appointment Payment table
     */
    @Modifying
    @Query(value = "DELETE FROM appt_pymt WHERE pymt_sts_code = 203 AND pymt_dt <= date(now())-interval '2 days'", nativeQuery = true)
    void cleanupCancelledPayments();

    /**
     * Get non-reconciled payment list for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @return List of appointment payments
     */
    @Query(value = "SELECT a FROM AppointmentPaymentEntity a WHERE a.paymentReconcileDate is null AND a.paymentStatusCode <> 201 " +
            "AND a.paymentDate BETWEEN :startDate AND :endDate ORDER BY a.paymentDate DESC")
    List<AppointmentPaymentEntity> getAllNonReconciledPaymentsForDateRange(@Param("startDate") LocalDate startDate,
                                                                           @Param("endDate") LocalDate endDate);

    /**
     * Get appointment payment list for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @return List of appointment payments
     */
    @Query(value = "SELECT a FROM AppointmentPaymentEntity a WHERE a.paymentDate BETWEEN :startDate AND :endDate ORDER BY a.paymentDate DESC")
    List<AppointmentPaymentEntity> getAllAppointmentPaymentsForDateRange(@Param("startDate") LocalDate startDate,
                                                                         @Param("endDate") LocalDate endDate);

    /**
     * Get appointment payment list for all Appointment Payment table data
     * @return List of appointment payments
     */
    @Query(value = "SELECT a FROM AppointmentPaymentEntity a ORDER BY a.paymentDate DESC")
    List<AppointmentPaymentEntity> getAllAppointmentPayments();

    /**
     * Get total of service amount (Pending & Processed) for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @return Total of service amount
     */
    @Query(value = "SELECT SUM(a.serviceAmount) FROM AppointmentPaymentEntity a WHERE a.paymentStatusCode <> 203 AND a.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalServiceAmountAll(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * Get total of BCI amount (Pending & Processed) for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @return Total of BCI amount
     */
    @Query(value = "SELECT SUM(a.bciAmount) FROM AppointmentPaymentEntity a WHERE a.paymentStatusCode <> 203 AND a.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalBciAmountAll(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    /**
     * Get total of service amount (Processed) for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @return Total of service amount
     */
    @Query(value = "SELECT SUM(a.serviceAmount) FROM AppointmentPaymentEntity a WHERE a.paymentStatusCode <> 201 AND a.paymentStatusCode <> 203 AND a.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalServiceAmountProcessed(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * Get total of BCI amount (Processed) for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @return Total of BCI amount
     */
    @Query(value = "SELECT SUM(a.bciAmount) FROM AppointmentPaymentEntity a WHERE a.paymentStatusCode <> 201 AND a.paymentStatusCode <> 203 AND a.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalBciAmountProcessed(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

}
