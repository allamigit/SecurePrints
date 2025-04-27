package com.secure_prints.service;

import com.secure_prints.database.entity.AppointmentInformationEntity;
import com.secure_prints.model.*;
import com.secure_prints.database.AppointmentInformationRepository;
import com.secure_prints.util.TimestampUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;

@Service
public class AppointmentInformationService {

    private final AppointmentInformationRepository appointmentInformationRepository;
    private int responseCode;
    private String responseMessage;

    /**
     * Constructor for AppointmentInformationService
     * @param appointmentInformationRepository appointmentInformationRepository
     */
    public AppointmentInformationService(AppointmentInformationRepository appointmentInformationRepository) {
        this.appointmentInformationRepository = appointmentInformationRepository;
    }

    /**
     * Schedule Appointment
     * @param appointmentRequest appointmentRequest
     * @return AppointmentResponse
     */
    public ApiResponse scheduleAppointment(AppointmentRequest appointmentRequest) {
        OffsetDateTime currentTimestamp = OffsetDateTime.now();
        String strAppointmentTimestamp = TimestampUtil.getTimestamp(appointmentRequest.getAppointmentDate(), appointmentRequest.getAppointmentTime());
        OffsetDateTime appointmentTimestamp = TimestampUtil.getOffsetDateTime(strAppointmentTimestamp);
        AppointmentResponse appointmentResponse = null;

        if(appointmentTimestamp.isBefore(currentTimestamp) || appointmentTimestamp.getDayOfWeek().equals(DayOfWeek.SATURDAY)
            || appointmentTimestamp.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            responseCode = 409;
            responseMessage = "Invalid given appointment date or time";
        } else {
            String serviceName = appointmentRequest.getServiceName();
            String serviceCode = ServiceType.getServiceCode(serviceName);
            BigDecimal serviceAmount = ServiceType.getServicePrice(serviceCode);
            String bciReasonCode = appointmentRequest.getBciReasonCode();
            String fbiReasonCode = appointmentRequest.getFbiReasonCode();
            if (appointmentRequest.getBciReasonCode() == null && appointmentRequest.getBciReasonText() != null) {
                assert serviceCode != null;
                bciReasonCode = serviceCode.equals(ServiceType.BCI.name()) || serviceCode.equals(ServiceType.BCI_FBI.name()) ?
                        ReasonService.getReasonCode(ServiceType.BCI.name(), appointmentRequest.getBciReasonText()) : null;
            }
            if (appointmentRequest.getFbiReasonCode() == null && appointmentRequest.getFbiReasonText() != null) {
                assert serviceCode != null;
                fbiReasonCode = serviceCode.equals(ServiceType.FBI.name()) || serviceCode.equals(ServiceType.BCI_FBI.name()) ?
                        ReasonService.getReasonCode(ServiceType.FBI.name(), appointmentRequest.getFbiReasonText()) : null;
            }

            long appointmentId = appointmentInformationRepository.getNextAppointmentId();
            AppointmentInformationEntity appointmentInformationEntity = AppointmentInformationEntity.builder()
                    .appointmentId(appointmentId)
                    .customerFirstName(appointmentRequest.getCustomerFirstName())
                    .customerLastName(appointmentRequest.getCustomerLastName())
                    .customerEmail(appointmentRequest.getCustomerEmail())
                    .customerPhone(appointmentRequest.getCustomerPhone())
                    .serviceCode(serviceCode)
                    .bciReasonCode(bciReasonCode)
                    .bciReasonText(appointmentRequest.getBciReasonText())
                    .fbiReasonCode(fbiReasonCode)
                    .fbiReasonText(appointmentRequest.getFbiReasonText())
                    .appointmentTimestamp(appointmentTimestamp)
                    .serviceAmount(serviceAmount)
                    .appointmentStatus(AppointmentStatus.Scheduled.getStatusCode())
                    .orderTimestamp(currentTimestamp)
                    .build();
            appointmentInformationRepository.save(appointmentInformationEntity);

            appointmentResponse = AppointmentResponse.builder()
                    .appointmentId(appointmentId)
                    .orderTimestamp(appointmentInformationEntity.getOrderTimestamp())
                    .serviceName(serviceName)
                    .bciReasonCode(bciReasonCode)
                    .bciReasonText(appointmentRequest.getBciReasonText())
                    .fbiReasonCode(fbiReasonCode)
                    .fbiReasonText(appointmentRequest.getFbiReasonText())
                    .appointmentTimestamp(appointmentInformationEntity.getAppointmentTimestamp())
                    .appointmentStatus(AppointmentStatus.Scheduled.name())
                    .statusTimestamp(currentTimestamp)
                    .customerFirstName(appointmentRequest.getCustomerFirstName())
                    .customerLastName(appointmentRequest.getCustomerLastName())
                    .customerEmail(appointmentRequest.getCustomerEmail())
                    .customerPhone(appointmentRequest.getCustomerPhone())
                    .build();

            responseCode = 201;
            responseMessage = "Appointment Scheduled";
        }

        ApiStatus apiStatus = ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
        ApiResponse apiResponse = ApiResponse.builder()
                .apiStatus(apiStatus)
                .apiResponse(appointmentResponse)
                .build();
        return apiResponse;
    }

    /**
     * Reschedule Appointment
     * @param appointmentId appointmentId
     * @param strAppointmentDate strAppointmentDate
     * @param strAppointmentTime strAppointmentTime
     * @return AppointmentResponse
     */
    public ApiResponse rescheduleAppointment(long appointmentId, String strAppointmentDate, String strAppointmentTime) {
        //TODO validate appointmentId, strAppointmentDate and strAppointmentTime for null values
        responseCode = 409;
        ApiStatus apiStatus;
        AppointmentResponse appointmentResponse = null;
        ApiResponse apiResponse;
        AppointmentInformationEntity appointmentInformationEntity = appointmentInformationRepository.findByAppointmentId(appointmentId);
        int appointmentStatus = 0;
        if(appointmentInformationEntity != null) {
            appointmentStatus = appointmentInformationEntity.getAppointmentStatus();
        }

        OffsetDateTime currentTimestamp = OffsetDateTime.now();
        String strAppointmentTimestamp = TimestampUtil.getTimestamp(strAppointmentDate, strAppointmentTime);
        OffsetDateTime appointmentTimestamp = TimestampUtil.getOffsetDateTime(strAppointmentTimestamp);
        if(appointmentInformationEntity == null) {
            responseMessage = "Appointment ID not found";
        } else if(appointmentTimestamp.isBefore(currentTimestamp) || appointmentTimestamp.getDayOfWeek().equals(DayOfWeek.SATURDAY)
               || appointmentTimestamp.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            responseMessage = "Invalid given appointment date or time";
        } else if(appointmentStatus == AppointmentStatus.Cancelled.getStatusCode()
               || appointmentStatus == AppointmentStatus.Completed.getStatusCode()) {
            responseMessage = "Invalid appointment status to Reschedule: " + AppointmentStatus.getStatusName(appointmentStatus);
        } else {
            appointmentInformationRepository.rescheduleAppointment(appointmentId, appointmentTimestamp, currentTimestamp);
            responseCode = 200;
            responseMessage = "Appointment Rescheduled";
            appointmentResponse = AppointmentResponse.builder()
                    .appointmentId(appointmentId)
                    .orderTimestamp(appointmentInformationEntity.getOrderTimestamp())
                    .serviceName(ServiceType.getServiceName(appointmentInformationEntity.getServiceCode()))
                    .bciReasonCode(appointmentInformationEntity.getBciReasonCode())
                    .bciReasonText(appointmentInformationEntity.getBciReasonText())
                    .fbiReasonCode(appointmentInformationEntity.getFbiReasonCode())
                    .fbiReasonText(appointmentInformationEntity.getFbiReasonText())
                    .appointmentTimestamp(appointmentTimestamp)
                    .appointmentStatus(AppointmentStatus.Rescheduled.name())
                    .statusTimestamp(currentTimestamp)
                    .customerFirstName(appointmentInformationEntity.getCustomerFirstName())
                    .customerLastName(appointmentInformationEntity.getCustomerLastName())
                    .customerEmail(appointmentInformationEntity.getCustomerEmail())
                    .customerPhone(appointmentInformationEntity.getCustomerPhone())
                    .build();
        }

        apiStatus = ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
        apiResponse = ApiResponse.builder()
                .apiStatus(apiStatus)
                .apiResponse(appointmentResponse)
                .build();
        return apiResponse;
    }

    /**
     * Cancel Appointment
     * @param appointmentId appointmentId
     * @return AppointmentResponse
     */
    public ApiResponse cancelAppointment(long appointmentId) {
        //TODO validate appointmentId for null value
        responseCode = 409;
        ApiStatus apiStatus;
        AppointmentResponse appointmentResponse = null;
        ApiResponse apiResponse;
        AppointmentInformationEntity appointmentInformationEntity = appointmentInformationRepository.findByAppointmentId(appointmentId);
        int appointmentStatus = 0;
        if(appointmentInformationEntity != null) {
            appointmentStatus = appointmentInformationEntity.getAppointmentStatus();
        }
        if(appointmentInformationEntity == null) {
            responseMessage = "Appointment ID not found";
        } else if(appointmentStatus == AppointmentStatus.Cancelled.getStatusCode()
                || appointmentStatus == AppointmentStatus.Completed.getStatusCode()) {
            responseMessage = "Invalid appointment status to Cancel: " + AppointmentStatus.getStatusName(appointmentStatus);
        } else {
            OffsetDateTime currentTimestamp = OffsetDateTime.now();
            appointmentInformationRepository.cancelAppointment(appointmentId, currentTimestamp);
            responseCode = 200;
            responseMessage = "Appointment Cancelled";
            appointmentResponse = AppointmentResponse.builder()
                    .appointmentId(appointmentId)
                    .orderTimestamp(appointmentInformationEntity.getOrderTimestamp())
                    .serviceName(ServiceType.getServiceName(appointmentInformationEntity.getServiceCode()))
                    .bciReasonCode(appointmentInformationEntity.getBciReasonCode())
                    .bciReasonText(appointmentInformationEntity.getBciReasonText())
                    .fbiReasonCode(appointmentInformationEntity.getFbiReasonCode())
                    .fbiReasonText(appointmentInformationEntity.getFbiReasonText())
                    .appointmentTimestamp(appointmentInformationEntity.getAppointmentTimestamp())
                    .appointmentStatus(AppointmentStatus.Cancelled.name())
                    .statusTimestamp(currentTimestamp)
                    .customerFirstName(appointmentInformationEntity.getCustomerFirstName())
                    .customerLastName(appointmentInformationEntity.getCustomerLastName())
                    .customerEmail(appointmentInformationEntity.getCustomerEmail())
                    .customerPhone(appointmentInformationEntity.getCustomerPhone())
                    .build();
        }

        apiStatus = ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
        apiResponse = ApiResponse.builder()
                .apiStatus(apiStatus)
                .apiResponse(appointmentResponse)
                .build();
        return apiResponse;
    }

}
