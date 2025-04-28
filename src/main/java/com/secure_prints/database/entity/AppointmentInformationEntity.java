package com.secure_prints.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
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
    private Long appointmentId;

    @Column(name = "cust_first_name")
    private String customerFirstName;

    @Column(name = "cust_last_name")
    private String customerLastName;

    @Column(name = "cust_email")
    private String customerEmail;

    @Column(name = "cust_phone")
    private String customerPhone;

    @Column(name = "svc_code")
    private String serviceCode;

    @Column(name = "bci_rsn_code")
    private String bciReasonCode;

    @Column(name = "bci_rsn_text")
    private String bciReasonText;

    @Column(name = "fbi_rsn_code")
    private String fbiReasonCode;

    @Column(name = "fbi_rsn_text")
    private String fbiReasonText;

    @Column(name = "appt_ts")
    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    private OffsetDateTime appointmentTimestamp;

    @Column(name = "svc_amt")
    private BigDecimal serviceAmount;

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

}
