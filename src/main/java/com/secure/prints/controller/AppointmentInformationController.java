package com.secure.prints.controller;

import com.secure.prints.database.entity.AppointmentInformationEntity;
import com.secure.prints.model.ApiResponse;
import com.secure.prints.model.Appointment;
import com.secure.prints.model.AppointmentRequest;
import com.secure.prints.model.AppointmentTime;
import com.secure.prints.service.AppointmentInformationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.List;

@RestController
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
                                           @RequestBody AppointmentRequest appointmentRequest) throws UnknownHostException {
        ApiResponse apiResponse = appointmentInformationService.scheduleAppointment(appointmentRequest);
        response.setStatus(apiResponse.getApiStatus().getResponseCode());
        return apiResponse;
    }

    /**
     * Reschedule Appointment
     * @param appointmentId appointmentId
     * @param strAppointmentTimestamp strAppointmentTimestamp
     * @return ApiResponse
     */
    @PatchMapping(value = "reschedule-appointment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse rescheduleAppointment(HttpServletResponse response,
                                             @RequestParam(name = "appointmentId") String appointmentId,
                                             @RequestParam(name = "appointmentTimestamp") String strAppointmentTimestamp) {
        ApiResponse apiResponse = appointmentInformationService.rescheduleAppointment(appointmentId, strAppointmentTimestamp);
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
                                         @RequestParam(name = "appointmentId") String appointmentId) {
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
                                           @RequestParam(name = "appointmentId") String appointmentId,
                                           @RequestParam(name = "paymentMethodName") String paymentMethodName) {
        ApiResponse apiResponse = appointmentInformationService.completeAppointment(appointmentId, paymentMethodName);
        response.setStatus(apiResponse.getApiStatus().getResponseCode());
        return apiResponse;
    }

    /**
     * Get appointment details by appointment ID
     * @param appointmentId appointmentId
     * @return ApiResponse
     */
    @GetMapping(value = "appointment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse getAppointmentDetails(HttpServletResponse response,
                                             @RequestParam(name = "appointmentId") String appointmentId) {
        ApiResponse apiResponse = appointmentInformationService.getAppointmentDetails(appointmentId);
        response.setStatus(apiResponse.getApiStatus().getResponseCode());
        return apiResponse;
    }

    /**
     * Find appointment details by appointment ID
     * @param appointmentId appointmentId
     * @return TRUE = Found / FALSE = Not Found
     */
    @GetMapping(value = "find-appointment-id", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean findAppointmentById(@RequestParam(name = "appointmentId") String appointmentId) {
        return appointmentInformationService.findAppointmentById(appointmentId);
    }

    /**
     * Find appointment by customer first and last name
     * @param customerFirstName customerFirstName
     * @param customerLastName customerLastName
     * @return appointmentId
     */
    @GetMapping(value = "find-appointment-name", produces = MediaType.APPLICATION_JSON_VALUE)
    public String findAppointmentByCustomerName(@RequestParam(name = "customerFirstName") String customerFirstName,
                                                @RequestParam(name = "customerLastName") String customerLastName) {
        return appointmentInformationService.findAppointmentByCustomerName(customerFirstName, customerLastName);
    }

    /**
     * Get list of all appointments or appointments for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @return List of appointments
     */
    @GetMapping(value = "all-appointments", produces = MediaType.APPLICATION_JSON_VALUE)
    public Appointment getAllAppointments(@RequestParam(name = "startDate", required = false) LocalDate startDate,
                                          @RequestParam(name = "endDate", required = false) LocalDate endDate) {
        return appointmentInformationService.getAllAppointments(startDate, endDate);
    }

    /**
     * Generate all available appointments list for a specific date
     * @param selectedDate selectedDate
     * @return List of available appointments
     */
    @GetMapping(value = "times-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AppointmentTime> generateAppointmentTimes(@RequestParam(name = "selectedDate") LocalDate selectedDate) {
        return appointmentInformationService.generateAppointmentTimes(selectedDate);
    }

}
