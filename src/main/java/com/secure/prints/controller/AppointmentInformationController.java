package com.secure.prints.controller;

import com.secure.prints.database.entity.AppointmentInformationEntity;
import com.secure.prints.model.ApiResponse;
import com.secure.prints.model.AppointmentRequest;
import com.secure.prints.model.AppointmentTime;
import com.secure.prints.service.AppointmentInformationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
     * @return ApiResponse
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
     * @return ApiResponse
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
     * @return ApiResponse
     */
    @PatchMapping(value = "cancel-appointment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse cancelAppointment(HttpServletResponse response,
                                         @RequestParam(name = "appointment-id") long appointmentId) {
        ApiResponse apiResponse = appointmentInformationService.cancelAppointment(appointmentId);
        response.setStatus(apiResponse.getApiStatus().getResponseCode());
        return apiResponse;
    }

    /**
     * Update appointment status to Completed and add payment entry
     * @param appointmentId appointmentId
     * @param paymentMethodName paymentMethodName
     * @return ApiResponse
     */
    @PostMapping(value = "complete-appointment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse completeAppointment(HttpServletResponse response,
                                           @RequestParam(name = "appointment-id") long appointmentId,
                                           @RequestParam(name = "payment-method") String paymentMethodName) {
        ApiResponse apiResponse = appointmentInformationService.completeAppointment(appointmentId, paymentMethodName);
        response.setStatus(apiResponse.getApiStatus().getResponseCode());
        return apiResponse;
    }

    /**
     * Get appointment details
     * @param appointmentId appointmentId
     * @return AppointmentInformationEntity
     */
    @GetMapping(value = "appointment", produces = MediaType.APPLICATION_JSON_VALUE)
    public AppointmentInformationEntity getAppointmentDetails(@RequestParam(name = "appointment-id") long appointmentId) {
        return appointmentInformationService.getAppointmentDetails(appointmentId);
    }

    /**
     * Get list of all appointments or appointments for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @param showByAppointmentDate showByAppointmentDate
     * @return List of appointments
     */
    @GetMapping(value = "all-appointments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AppointmentInformationEntity> getAllAppointments(@RequestParam(name = "start-date", required = false) LocalDate startDate,
                                                                 @RequestParam(name = "end-date", required = false) LocalDate endDate,
                                                                 @RequestParam(name = "show-by-appointment-date", required = false) boolean showByAppointmentDate) {
        return appointmentInformationService.getAllAppointments(startDate, endDate, showByAppointmentDate);
    }

    /**
     * Get all available appointments list for a specific date
     * @param selectedDate selectedDate
     * @return List of available appointments
     */
    @GetMapping(value = "times-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AppointmentTime> getAppointmentTimes(@RequestParam(name = "selected-date") LocalDate selectedDate) {
        return appointmentInformationService.getAppointmentTimes(selectedDate);
    }

}
