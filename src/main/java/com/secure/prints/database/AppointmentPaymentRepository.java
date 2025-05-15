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
    @Query(value = "UPDATE AppointmentPaymentEntity a SET a.paymentReconcileDate = :reconcileDate " +
            "WHERE a.appointmentId = :appointmentId")
    void reconcilePayment(@Param("appointmentId") String appointmentId,
                          @Param("reconcileDate") LocalDate reconcileDate);

    /**
     * Update payment status and date
     * @param appointmentId appointmentId
     * @param paymentStatusCode paymentStatusCode
     */
    @Modifying
    @Query(value = "UPDATE AppointmentPaymentEntity a SET a.paymentStatusCode = :paymentStatusCode, a.paymentDate = :currentDate " +
            "WHERE a.appointmentId = :appointmentId")
    void updatePaymentStatus(@Param("appointmentId") String appointmentId,
                             @Param("paymentStatusCode") int paymentStatusCode,
                             @Param("currentDate") LocalDate currentDate);

    /**
     * Update payment status, method and date
     * @param appointmentId appointmentId
     * @param paymentStatusCode paymentStatusCode
     * @param paymentMethodCode paymentMethodCode
     */
    @Modifying
    @Query(value = "UPDATE AppointmentPaymentEntity a SET a.paymentStatusCode = :paymentStatusCode, a.paymentMethodCode = :paymentMethodCode, " +
            "a.serviceAmount = a.serviceAmount - :transactionFees, a.paymentDate = :currentDate " +
            "WHERE a.appointmentId = :appointmentId")
    void updatePaymentStatusAndMethod(@Param("appointmentId") String appointmentId,
                                      @Param("paymentStatusCode") int paymentStatusCode,
                                      @Param("paymentMethodCode") int paymentMethodCode,
                                      @Param("transactionFees") BigDecimal transactionFees,
                                      @Param("currentDate") LocalDate currentDate);

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
     * @return List of not reconciled payments
     */
    @Query(value = "SELECT a FROM AppointmentPaymentEntity a WHERE a.paymentReconcileDate is null AND " +
            "a.paymentDate BETWEEN :startDate AND :endDate ORDER BY a.paymentDate DESC")
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

}
