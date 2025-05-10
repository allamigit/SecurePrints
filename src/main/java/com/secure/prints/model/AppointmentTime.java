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
public class AppointmentTime implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String appointmentTimeLabel;
    private String appointmentTimestamp;

}
