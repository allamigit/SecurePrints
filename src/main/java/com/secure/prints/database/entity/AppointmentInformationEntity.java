package com.secure.prints.database.entity;

import com.secure.prints.config.EncryptionConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appt_info")
public class AppointmentInformationEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "appt_id")
    private String appointmentId;

    @Column(name = "cust_first_name")
    @Convert(converter = EncryptionConverter.class)
    private String customerFirstName;

    @Column(name = "cust_last_name")
    @Convert(converter = EncryptionConverter.class)
    private String customerLastName;

    @Column(name = "cust_email")
    @Convert(converter = EncryptionConverter.class)
    private String customerEmail;

    @Column(name = "cust_phone")
    @Convert(converter = EncryptionConverter.class)
    private String customerPhone;

    @Column(name = "svc_code")
    private String serviceCode;

    @Column(name = "bci_rsn_code")
    private String bciReasonCode;

    @Column(name = "bci_rsn_desc")
    private String bciReasonDescription;

    @Column(name = "fbi_rsn_code")
    private String fbiReasonCode;

    @Column(name = "fbi_rsn_desc")
    private String fbiReasonDescription;

    @Column(name = "appt_ts")
    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    private OffsetDateTime appointmentTimestamp;

    @Column(name = "cncl_appt_ts")
    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    private OffsetDateTime cancelledAppointmentTimestamp;

    @Column(name = "appt_sts_code")
    private Integer appointmentStatusCode;

    @Column(name = "ordr_ts")
    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    private OffsetDateTime orderTimestamp;

    @Column(name = "rsch_ts")
    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    private OffsetDateTime resheduleTimestamp;

    @Column(name = "cncl_ts")
    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    private OffsetDateTime cancelTimestamp;

    @Column(name = "cmpl_ts")
    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    private OffsetDateTime completeTimestamp;

    @Column(name = "usr_ip")
    @Convert(converter = EncryptionConverter.class)
    private String userIpAddress;

}
