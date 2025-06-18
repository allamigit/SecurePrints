package com.secure.prints.model;

import com.secure.prints.database.entity.AppointmentInformationEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<AppointmentInformationEntity> appointmentInformation;
    private List<AppointmentResponse> appointmentResponse;

}
