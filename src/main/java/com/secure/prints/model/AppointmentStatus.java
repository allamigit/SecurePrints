package com.secure.prints.model;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Generated
public enum AppointmentStatus {

    Scheduled(101),
    Rescheduled(102),
    Cancelled(103),
    Completed(104);

    private final int statusCode;

    public static int getStatusCode(String status) {
        for(AppointmentStatus sts : AppointmentStatus.values()) {
            if(sts.name().equals(status)) {
                return sts.getStatusCode();
            }
        }
        return 0;
    }

    public static String getStatusName(int code) {
        for(AppointmentStatus sts : AppointmentStatus.values()) {
            if(sts.getStatusCode() == code) {
                return sts.name();
            }
        }
        return null;
    }

}
