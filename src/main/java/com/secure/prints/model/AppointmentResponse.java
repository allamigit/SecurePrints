package com.secure.prints.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String appointmentId;
    private String orderTimestamp;
    private String serviceName;
    private String bciReasonCode;
    private String bciReasonDescription;
    private String fbiReasonCode;
    private String fbiReasonDescription;
    private String appointmentTimestamp;
    private String appointmentStatus;
    private String statusTimestamp;

}
