package com.secure.prints.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.OffsetDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long appointmentId;
    private OffsetDateTime orderTimestamp;
    private String serviceName;
    private String bciReasonCode;
    private String bciReasonText;
    private String fbiReasonCode;
    private String fbiReasonText;
    private OffsetDateTime appointmentTimestamp;
    private String appointmentStatus;
    private OffsetDateTime statusTimestamp;
    private String customerFirstName;
    private String customerLastName;
    private String customerEmail;
    private String customerPhone;

}
