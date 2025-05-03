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
public interface AppointmentInformationRepository extends JpaRepository<AppointmentInformationEntity, Long> {

    /**
     * Get appointment details by appointment ID
     * @param appointmentId appointmentId
     * @return AppointmentInformationEntity
     */
    AppointmentInformationEntity findByAppointmentId(long appointmentId);


    /**
     * Update appointment status as Rescheduled
     * @param appointmentId appointmentId
     * @param appointmentTimestamp appointmentTimestamp
     * @param currentTimestamp currentTimestamp
     */
    @Modifying
    @Query(value = "UPDATE AppointmentInformationEntity a SET a.appointmentTimestamp = :appointmentTimestamp, " +
            "a.resheduleTimestamp = :currentTimestamp, a.appointmentStatusCode = 102 WHERE a.appointmentId = :appointmentId")
    void rescheduleAppointment(@Param("appointmentId") long appointmentId,
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
    void cancelAppointment(@Param("appointmentId") long appointmentId,
                           @Param("currentTimestamp") OffsetDateTime currentTimestamp);

    /**
     * Update appointment status as Completed
     * @param appointmentId appointmentId
     * @param currentTimestamp currentTimestamp
     */
    @Modifying
    @Query(value = "UPDATE AppointmentInformationEntity a SET a.completeTimestamp = :currentTimestamp, " +
            "a.appointmentStatusCode = 104 WHERE a.appointmentId = :appointmentId")
    void completeAppointment(@Param("appointmentId") long appointmentId,
                             @Param("currentTimestamp") OffsetDateTime currentTimestamp);

    /**
     * Get next value of appointment sequence
     * @return nextAppointmentId
     */
    @Query(value = "SELECT nextval('appt_info_seq')")
    long getNextAppointmentId();

    /**
     * Get appointment list for a specific date range
     * @param startTimestamp startTimestamp
     * @param endTimestamp endTimestamp
     * @return List of appointments
     */
    @Query(value = "SELECT a FROM AppointmentInformationEntity a WHERE a.orderTimestamp BETWEEN :startTimestamp AND :endTimestamp ORDER BY a.orderTimestamp DESC")
    List<AppointmentInformationEntity> getAllAppointmentsForDateRange(@Param("startTimestamp") OffsetDateTime startTimestamp,
                                                                      @Param("endTimestamp") OffsetDateTime endTimestamp);

    /**
     * Get appointment times list for a specific date range
     * @param startTimestamp startTimestamp
     * @param endTimestamp endTimestamp
     * @return List of appointment times
     */
    @Query(value = "SELECT a FROM AppointmentInformationEntity a WHERE a.appointmentTimestamp BETWEEN :startTimestamp AND :endTimestamp ORDER BY a.appointmentTimestamp ASC")
    List<AppointmentInformationEntity> getAppointmentTimesForDateRange(@Param("startTimestamp") OffsetDateTime startTimestamp,
                                                                       @Param("endTimestamp") OffsetDateTime endTimestamp);
    /**
     * Get appointment list for all appointment table data
     * @return List of appointments
     */
    @Query(value = "SELECT a FROM AppointmentInformationEntity a ORDER BY a.orderTimestamp DESC")
    List<AppointmentInformationEntity> getAllAppointments();

}
