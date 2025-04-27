package com.secure_prints.database;

import com.secure_prints.database.entity.AppointmentInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Repository
@Transactional
public interface AppointmentInformationRepository extends JpaRepository<AppointmentInformationEntity, Long> {

    AppointmentInformationEntity findByAppointmentId(long appointmentId);

    @Modifying
    @Query(value = "UPDATE AppointmentInformationEntity a SET a.appointmentTimestamp = :appointmentTimestamp, " +
            "a.resheduleTimestamp = :currentTimestamp, a.appointmentStatus = 102 WHERE a.appointmentId = :appointmentId")
    void rescheduleAppointment(@Param("appointmentId") long appointmentId,
                               @Param("appointmentTimestamp") OffsetDateTime appointmentTimestamp,
                               @Param("currentTimestamp") OffsetDateTime currentTimestamp);

    @Modifying
    @Query(value = "UPDATE AppointmentInformationEntity a SET a.cancelTimestamp = :currentTimestamp, " +
            "a.appointmentStatus = 103 WHERE a.appointmentId = :appointmentId")
    void cancelAppointment(@Param("appointmentId") long appointmentId,
                           @Param("currentTimestamp") OffsetDateTime currentTimestamp);

    @Modifying
    @Query(value = "UPDATE AppointmentInformationEntity a SET a.completeTimestamp = :currentTimestamp, " +
            "a.appointmentStatus = 104 WHERE a.appointmentId = :appointmentId")
    void completeAppointment(@Param("appointmentId") long appointmentId,
                             @Param("currentTimestamp") OffsetDateTime currentTimestamp);

    @Query(value = "SELECT nextval('appt_info_seq')")
    long getNextAppointmentId();

}
