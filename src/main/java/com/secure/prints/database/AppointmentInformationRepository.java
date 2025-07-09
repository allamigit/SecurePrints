package com.secure.prints.database;

import com.secure.prints.database.entity.AppointmentInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
@Transactional
public interface AppointmentInformationRepository extends JpaRepository<AppointmentInformationEntity, String> {

    /**
     * Get appointment details by appointment ID
     * @param appointmentId appointmentId
     * @return AppointmentInformationEntity
     */
    AppointmentInformationEntity findByAppointmentId(String appointmentId);


    /**
     * Update appointment status as Rescheduled
     * @param appointmentId appointmentId
     * @param appointmentTimestamp appointmentTimestamp
     * @param currentTimestamp currentTimestamp
     */
    @Modifying
    @Query(value = "UPDATE AppointmentInformationEntity a SET a.appointmentTimestamp = :appointmentTimestamp, " +
            "a.resheduleTimestamp = :currentTimestamp, a.appointmentStatusCode = 102 WHERE a.appointmentId = :appointmentId")
    void rescheduleAppointment(@Param("appointmentId") String appointmentId,
                               @Param("appointmentTimestamp") OffsetDateTime appointmentTimestamp,
                               @Param("currentTimestamp") OffsetDateTime currentTimestamp);


    /**
     * Update appointment status as Cancelled
     * @param appointmentId appointmentId
     * @param currentTimestamp currentTimestamp
     */
    @Modifying
    @Query(value = "UPDATE AppointmentInformationEntity a SET a.cancelTimestamp = :currentTimestamp, " +
            "a.appointmentStatusCode = 103 WHERE a.appointmentId = :appointmentId")
    void cancelAppointment(@Param("appointmentId") String appointmentId,
                           @Param("currentTimestamp") OffsetDateTime currentTimestamp);

    /**
     * Update appointment status as Completed
     * @param appointmentId appointmentId
     * @param completeTimestamp completeTimestamp
     */
    @Modifying
    @Query(value = "UPDATE AppointmentInformationEntity a SET a.completeTimestamp = :completeTimestamp, " +
            "a.appointmentStatusCode = 104 WHERE a.appointmentId = :appointmentId")
    void completeAppointment(@Param("appointmentId") String appointmentId,
                             @Param("completeTimestamp") OffsetDateTime completeTimestamp);

    /**
     * Cleanup Cancelled appointments from Appointment Information table
     */
    @Modifying
    @Query(value = "DELETE FROM appt_info WHERE appt_sts_code = 103 AND cncl_ts <= date(now())-interval '2 days'", nativeQuery = true)
    void cleanupCancelledAppointments();

    /**
     * Get next value of appointment sequence
     * @return nextAppointmentId
     */
    @Query(value = "SELECT nextval('appt_info_seq')")
    long getNextAppointmentId();

    /**
     * Get appointment list for a specific date range for appointment time
     * @param startTimestamp startTimestamp
     * @param endTimestamp endTimestamp
     * @return List of appointments
     */
    @Query(value = "SELECT a FROM AppointmentInformationEntity a WHERE a.appointmentTimestamp BETWEEN :startTimestamp AND :endTimestamp ORDER BY a.appointmentTimestamp ASC")
    List<AppointmentInformationEntity> getAllAppointmentsForDateRange(@Param("startTimestamp") OffsetDateTime startTimestamp,
                                                                      @Param("endTimestamp") OffsetDateTime endTimestamp);

    /**
     * Get appointment list for all Appointment Information table data ordered by appointment time
     * @return List of appointments
     */
    @Query(value = "SELECT a FROM AppointmentInformationEntity a ORDER BY a.appointmentTimestamp ASC")
    List<AppointmentInformationEntity> getAllAppointments();

    /**
     * Get active appointment list (Scheduled & Rescheduled) for a specific date range for appointment time
     * @param startTimestamp startTimestamp
     * @param endTimestamp endTimestamp
     * @return List of active appointment times
     */
    @Query(value = "SELECT a FROM AppointmentInformationEntity a WHERE (a.appointmentStatusCode = 101 OR a.appointmentStatusCode = 102) AND " +
            "a.appointmentTimestamp BETWEEN :startTimestamp AND :endTimestamp ORDER BY a.appointmentTimestamp ASC")
    List<AppointmentInformationEntity> getActiveAppointmentTimesForDateRange(@Param("startTimestamp") OffsetDateTime startTimestamp,
                                                                             @Param("endTimestamp") OffsetDateTime endTimestamp);
    /**
     * Check appointment if it is duplicate
     * @param customerFirstName customerFirstName
     * @param customerLastName customerLastName
     * @param serviceCode serviceCode
     * @return TRUE/FALSE
     */
    @Query(value = "SELECT a FROM AppointmentInformationEntity a WHERE a.customerFirstName = :customerFirstName AND a.customerLastName = :customerLastName " +
            "AND a.serviceCode = :serviceCode AND a.appointmentStatusCode BETWEEN 101 AND 102")
    AppointmentInformationEntity checkDuplicateAppointment(@Param("customerFirstName") String customerFirstName,
                                                           @Param("customerLastName") String customerLastName,
                                                           @Param("serviceCode") String serviceCode);

    /**
     * Find appointment by customer first and last name
     * @param customerFirstName customerFirstName
     * @param customerLastName customerLastName
     * @return TRUE/FALSE
     */
    @Query(value = "SELECT a FROM AppointmentInformationEntity a WHERE a.customerFirstName = :customerFirstName AND a.customerLastName = :customerLastName " +
            "AND a.appointmentStatusCode BETWEEN 101 AND 102")
    AppointmentInformationEntity findAppointmentByCustomerName(@Param("customerFirstName") String customerFirstName,
                                                               @Param("customerLastName") String customerLastName);

}
