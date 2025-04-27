package com.secure_prints.controller;

import com.secure_prints.model.ApiResponse;
import com.secure_prints.model.AppointmentRequest;
import com.secure_prints.model.AppointmentResponse;
import com.secure_prints.service.AppointmentInformationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.OffsetDateTime;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AppointmentInformationController {

    private final AppointmentInformationService appointmentInformationService;

    /**
     * Constructor for AppointmentInformationController
     * @param appointmentInformationService appointmentInformationService
     */
    public AppointmentInformationController(AppointmentInformationService appointmentInformationService) {
        this.appointmentInformationService = appointmentInformationService;
    }

    /**
     * Schedule Appointment
     * @param appointmentRequest appointmentRequest
     * @return AppointmentResponse
     */
    @PostMapping(value = "schedule-appointment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse scheduleAppointment(HttpServletResponse response,
                                           @RequestBody AppointmentRequest appointmentRequest) {
        ApiResponse apiResponse = appointmentInformationService.scheduleAppointment(appointmentRequest);
        response.setStatus(apiResponse.getApiStatus().getResponseCode());
        return apiResponse;
    }

    /**
     * Reschedule Appointment
     * @param appointmentId appointmentId
     * @param strAppointmentDate strAppointmentDate
     * @param strAppointmentTime strAppointmentTime
     * @return AppointmentResponse
     */
    @PatchMapping(value = "reschedule-appointment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse rescheduleAppointment(HttpServletResponse response,
                                             @RequestParam(name = "appointment-id") long appointmentId,
                                             @RequestParam(name = "appointment-date") String strAppointmentDate,
                                             @RequestParam(name = "appointment-time") String strAppointmentTime) {
        ApiResponse apiResponse =  appointmentInformationService.rescheduleAppointment(appointmentId, strAppointmentDate, strAppointmentTime);
        response.setStatus(apiResponse.getApiStatus().getResponseCode());
        return apiResponse;
    }

    /**
     * Cancel Appointment
     * @param appointmentId appointmentId
     * @return AppointmentResponse
     */
    @PatchMapping(value = "cancel-appointment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse cancelAppointment(HttpServletResponse response,
                                         @RequestParam(name = "appointment-id") long appointmentId) {
        ApiResponse apiResponse = appointmentInformationService.cancelAppointment(appointmentId);
        response.setStatus(apiResponse.getApiStatus().getResponseCode());
        return apiResponse;
    }

}
