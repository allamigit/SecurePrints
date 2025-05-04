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
public class AppointmentRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String customerFirstName;
    private String customerLastName;
    private String customerEmail;
    private String customerPhone;
    private String serviceName;
    private String bciReasonCode;
    private String bciReasonText;
    private String fbiReasonCode;
    private String fbiReasonText;
    private String appointmentTimestamp;

}
