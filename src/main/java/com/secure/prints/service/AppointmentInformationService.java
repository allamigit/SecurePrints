package com.secure.prints.service;

import com.secure.prints.database.AppointmentPaymentRepository;
import com.secure.prints.database.entity.AppointmentInformationEntity;
import com.secure.prints.database.entity.AppointmentPaymentEntity;
import com.secure.prints.model.*;
import com.secure.prints.database.AppointmentInformationRepository;
import com.secure.prints.util.TimestampUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentInformationService {

    private final AppointmentInformationRepository appointmentInformationRepository;
    private final AppointmentPaymentRepository appointmentPaymentRepository;
    private int responseCode;
    private String responseMessage;
    @Value("${secure-prints.appointment.cut-from-start-index}")
    private int cutFromStartIndex;
    @Value("${secure-prints.appointment.cut-from-end-index}")
    private int cutFromEndIndex;
    @Value("${secure-prints.appointment.start-hour}")
    private int startHour;
    @Value("${secure-prints.appointment.end-hour}")
    private int endHour;

    /**
     * Constructor for AppointmentInformationService
     * @param appointmentInformationRepository appointmentInformationRepository
     * @param appointmentPaymentRepository appointmentPaymentRepository
     */
    public AppointmentInformationService(AppointmentInformationRepository appointmentInformationRepository,
                                         AppointmentPaymentRepository appointmentPaymentRepository) {
        this.appointmentInformationRepository = appointmentInformationRepository;
        this.appointmentPaymentRepository = appointmentPaymentRepository;
    }

    /**
     * Schedule Appointment
     * @param appointmentRequest appointmentRequest
     * @return ApiResponse
     */
    public ApiResponse scheduleAppointment(AppointmentRequest appointmentRequest) {
        OffsetDateTime currentTimestamp = OffsetDateTime.now();
        OffsetDateTime appointmentTimestamp = TimestampUtil.getOffsetDateTime(appointmentRequest.getAppointmentTimestamp());
        AppointmentResponse appointmentResponse = this.checkDuplicateAppointment(appointmentRequest);

        if( appointmentResponse != null) {
            responseCode = 409;
            responseMessage = "Duplicate appointment for the same service was found and not processed yet";
        } else {
            String serviceName = appointmentRequest.getServiceName();
            String serviceCode = ServiceType.getServiceCode(serviceName);
            BigDecimal serviceAmount = ServiceType.getServiceFee(serviceCode);
            String bciReasonCode = appointmentRequest.getBciReasonCode();
            String fbiReasonCode = appointmentRequest.getFbiReasonCode();
            if (bciReasonCode == null && appointmentRequest.getBciReasonText() != null) {
                assert serviceCode != null;
                bciReasonCode = serviceCode.equals(ServiceType.BCI.name()) || serviceCode.equals(ServiceType.BCI_FBI.name()) ?
                        ReasonService.getReasonCode(ServiceType.BCI.name(), appointmentRequest.getBciReasonText()) : null;
            }
            if (fbiReasonCode == null && appointmentRequest.getFbiReasonText() != null) {
                assert serviceCode != null;
                fbiReasonCode = serviceCode.equals(ServiceType.FBI.name()) || serviceCode.equals(ServiceType.BCI_FBI.name()) ?
                        ReasonService.getReasonCode(ServiceType.FBI.name(), appointmentRequest.getFbiReasonText()) : null;
            }

            // Assign reason text values to save in appt_info table when reasonCode is NO ORC or null
            ReasonText reasonText = this.getReasonText(bciReasonCode, appointmentRequest.getBciReasonText(), fbiReasonCode, appointmentRequest.getFbiReasonText());
            // Get next sequence value for appt_info table primary key
            long appointmentId = appointmentInformationRepository.getNextAppointmentId();
            AppointmentInformationEntity appointmentInformationEntity = AppointmentInformationEntity.builder()
                    .appointmentId(appointmentId)
                    .customerFirstName(appointmentRequest.getCustomerFirstName())
                    .customerLastName(appointmentRequest.getCustomerLastName())
                    .customerEmail(appointmentRequest.getCustomerEmail())
                    .customerPhone(appointmentRequest.getCustomerPhone())
                    .serviceCode(serviceCode)
                    .bciReasonCode(bciReasonCode)
                    .bciReasonText(reasonText.getBciReasonText())
                    .fbiReasonCode(fbiReasonCode)
                    .fbiReasonText(reasonText.getFbiReasonText())
                    .appointmentTimestamp(appointmentTimestamp)
                    .serviceAmount(serviceAmount)
                    .appointmentStatusCode(AppointmentStatus.Scheduled.getStatusCode())
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
     * @param strAppointmentTimestamp strAppointmentTimestamp
     * @return ApiResponse
     */
    public ApiResponse rescheduleAppointment(long appointmentId, String strAppointmentTimestamp) {
        responseCode = 409;
        ApiStatus apiStatus;
        AppointmentResponse appointmentResponse = null;
        ApiResponse apiResponse;
        AppointmentInformationEntity appointmentInformationEntity = this.getAppointmentDetails(appointmentId);
        int appointmentStatusCode = 0;
        if(appointmentInformationEntity != null) {
            appointmentStatusCode = appointmentInformationEntity.getAppointmentStatusCode();
        }

        OffsetDateTime currentTimestamp = OffsetDateTime.now();
        OffsetDateTime appointmentTimestamp = TimestampUtil.getOffsetDateTime(strAppointmentTimestamp);
        String strNewAppointmentTs = appointmentTimestamp.toString().substring(0, appointmentTimestamp.toString().length() - 6) + "Z";
        if(appointmentInformationEntity == null) {
            responseMessage = "Appointment ID not found";
        } else if(this.isAppointmentStatusFinal(appointmentStatusCode)) {
            responseMessage = "Invalid appointment status to reschedule: " + AppointmentStatus.getStatusName(appointmentStatusCode);
        } else if(appointmentInformationEntity.getAppointmentTimestamp().toString().equals(strNewAppointmentTs)) {
            responseMessage = "The given appointment date and time are not changed";
        } else {
            appointmentInformationRepository.rescheduleAppointment(appointmentId, appointmentTimestamp, currentTimestamp);
            responseCode = 200;
            responseMessage = "Appointment Rescheduled";
            String serviceCode = appointmentInformationEntity.getServiceCode();
            String bciReasonCode = appointmentInformationEntity.getBciReasonCode();
            String fbiReasonCode = appointmentInformationEntity.getFbiReasonCode();
            appointmentResponse = AppointmentResponse.builder()
                    .appointmentId(appointmentId)
                    .orderTimestamp(appointmentInformationEntity.getOrderTimestamp())
                    .serviceName(ServiceType.getServiceName(serviceCode))
                    .bciReasonCode(appointmentInformationEntity.getBciReasonCode())
                    .bciReasonText(ReasonService.getReasonText("BCI", bciReasonCode))
                    .fbiReasonCode(appointmentInformationEntity.getFbiReasonCode())
                    .fbiReasonText(ReasonService.getReasonText("FBI", fbiReasonCode))
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
     * @return ApiResponse
     */
    public ApiResponse cancelAppointment(long appointmentId) {
        responseCode = 409;
        ApiStatus apiStatus;
        AppointmentResponse appointmentResponse = null;
        ApiResponse apiResponse;
        AppointmentInformationEntity appointmentInformationEntity = this.getAppointmentDetails(appointmentId);
        int appointmentStatusCode = 0;
        if(appointmentInformationEntity != null) {
            appointmentStatusCode = appointmentInformationEntity.getAppointmentStatusCode();
        }

        if(appointmentInformationEntity == null) {
            responseMessage = "Appointment ID not found";
        } else if(this.isAppointmentStatusFinal(appointmentStatusCode)) {
            responseMessage = "Invalid appointment status to cancel: " + AppointmentStatus.getStatusName(appointmentStatusCode);
        } else {
            OffsetDateTime currentTimestamp = OffsetDateTime.now();
            appointmentInformationRepository.cancelAppointment(appointmentId, currentTimestamp);
            responseCode = 200;
            responseMessage = "Appointment Cancelled";
            String serviceCode = appointmentInformationEntity.getServiceCode();
            String bciReasonCode = appointmentInformationEntity.getBciReasonCode();
            String fbiReasonCode = appointmentInformationEntity.getFbiReasonCode();
            appointmentResponse = AppointmentResponse.builder()
                    .appointmentId(appointmentId)
                    .orderTimestamp(appointmentInformationEntity.getOrderTimestamp())
                    .serviceName(ServiceType.getServiceName(serviceCode))
                    .bciReasonCode(appointmentInformationEntity.getBciReasonCode())
                    .bciReasonText(ReasonService.getReasonText("BCI", bciReasonCode))
                    .fbiReasonCode(appointmentInformationEntity.getFbiReasonCode())
                    .fbiReasonText(ReasonService.getReasonText("FBI", fbiReasonCode))
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

    /**
     * Update appointment status to Completed and add payment entry
     * @param appointmentId appointmentId
     * @param paymentMethodName paymentMethodName
     * @return ApiResponse
     */
    public ApiResponse completeAppointment(long appointmentId, String paymentMethodName) {
        //TODO implementation of completing appointment and add payment entry
        responseCode = 409;
        ApiStatus apiStatus;
        AppointmentResponse appointmentResponse = null;
        ApiResponse apiResponse;
        AppointmentInformationEntity appointmentInformationEntity = this.getAppointmentDetails(appointmentId);
        int appointmentStatusCode = 0;
        if(appointmentInformationEntity != null) {
            appointmentStatusCode = appointmentInformationEntity.getAppointmentStatusCode();
        }

        if(appointmentInformationEntity == null) {
            responseMessage = "Appointment ID not found";
        } else if(this.isAppointmentStatusFinal(appointmentStatusCode)) {
            responseMessage = "Invalid appointment status to complete: " + AppointmentStatus.getStatusName(appointmentStatusCode);
        } else {
            OffsetDateTime currentTimestamp = OffsetDateTime.now();
            appointmentInformationRepository.completeAppointment(appointmentId, currentTimestamp);
            responseCode = 200;
            responseMessage = "Appointment Completed";
            String serviceCode = appointmentInformationEntity.getServiceCode();
            String bciReasonCode = appointmentInformationEntity.getBciReasonCode();
            String fbiReasonCode = appointmentInformationEntity.getFbiReasonCode();
            appointmentResponse = AppointmentResponse.builder()
                    .appointmentId(appointmentId)
                    .orderTimestamp(appointmentInformationEntity.getOrderTimestamp())
                    .serviceName(ServiceType.getServiceName(serviceCode))
                    .bciReasonCode(appointmentInformationEntity.getBciReasonCode())
                    .bciReasonText(ReasonService.getReasonText("BCI", bciReasonCode))
                    .fbiReasonCode(appointmentInformationEntity.getFbiReasonCode())
                    .fbiReasonText(ReasonService.getReasonText("FBI", fbiReasonCode))
                    .appointmentTimestamp(appointmentInformationEntity.getAppointmentTimestamp())
                    .appointmentStatus(AppointmentStatus.Completed.name())
                    .statusTimestamp(currentTimestamp)
                    .customerFirstName(appointmentInformationEntity.getCustomerFirstName())
                    .customerLastName(appointmentInformationEntity.getCustomerLastName())
                    .customerEmail(appointmentInformationEntity.getCustomerEmail())
                    .customerPhone(appointmentInformationEntity.getCustomerPhone())
                    .build();

            // Add payment entry to appointment payment table
            AppointmentPaymentEntity appointmentPaymentEntity = AppointmentPaymentEntity.builder()
                    .appointmentId(String.valueOf(appointmentId))
                    .serviceCode(serviceCode)
                    .serviceAmount(appointmentInformationEntity.getServiceAmount())
                    .paymentStatusCode(PaymentStatus.Processed.getPaymentStatusCode())
                    .paymentMethodCode(PaymentMethod.getPaymentMethodCode(paymentMethodName))
                    .paymentDate(LocalDate.now())
                    .build();
            appointmentPaymentRepository.save(appointmentPaymentEntity);
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
     * Get appointment details by appointment ID
     * @param appointmentId appointmentId
     * @return AppointmentInformationEntity
     */
    public AppointmentInformationEntity getAppointmentDetails(long appointmentId) {
        return appointmentInformationRepository.findByAppointmentId(appointmentId);
    }

    /**
     * Get list of all appointments or appointments for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @param showByAppointmentDate showByAppointmentDate
     * @return List of appointments
     */
    public List<AppointmentInformationEntity> getAllAppointments(LocalDate startDate, LocalDate endDate, boolean showByAppointmentDate) {
        List<AppointmentInformationEntity> resultList = null;
        if(startDate != null && endDate != null) {
            DateRange dateRange = TimestampUtil.getOffsetDateRange(startDate, endDate);
            if(showByAppointmentDate) {
                resultList = appointmentInformationRepository.getAppointmentTimesForDateRange(dateRange.getStartTimestamp(), dateRange.getEndTimestamp());
            } else {
                resultList = appointmentInformationRepository.getAllAppointmentsForDateRange(dateRange.getStartTimestamp(), dateRange.getEndTimestamp());
            }
        } else if(startDate == null && endDate == null) {
            resultList = appointmentInformationRepository.getAllAppointments();
        }
        return resultList;
    }

    /**
     * Get all available appointments list for a specific date
     * @param selectedDate selectedDate
     * @return List of available appointments
     */
    public List<AppointmentTime> getAppointmentTimes(LocalDate selectedDate) {
        String strDate = selectedDate.toString() + " ";
        List<AppointmentTime> timeList = new ArrayList<>();
        DateRange dateRange = TimestampUtil.getOffsetDateRange(selectedDate, selectedDate);
        if(TimestampUtil.isValidTimestamp(dateRange.getEndTimestamp())) {
            return timeList;
        }

        // Create Time List
        StringBuilder timeLabel;
        StringBuilder apptTs;
        for(int hour = startHour; hour <= endHour; hour++) {
            for(int minute = 0; minute < 60; minute = minute + 15) {
                timeLabel = new StringBuilder(hour > 12 ? String.valueOf(hour - 12) : String.valueOf(hour));
                apptTs = new StringBuilder(hour < 10 ? "0" + hour : String.valueOf(hour));
                if(minute == 0) {
                    timeLabel.append(":00");
                    apptTs.append(":00:00");
                } else {
                    timeLabel.append(":").append(minute);
                    apptTs.append(":").append(minute).append(":00");
                }
                timeLabel = new StringBuilder(hour > 11 ? timeLabel + " PM" : timeLabel + " AM");
                timeList.add(new AppointmentTime(timeLabel.toString(), strDate + apptTs));
            }
        }

        if(dateRange.getStartTimestamp().getDayOfWeek() == DayOfWeek.FRIDAY && cutFromEndIndex > 0) {
            timeList.subList(cutFromEndIndex, timeList.size()).clear();
        }

        if(cutFromStartIndex > 0) {
            timeList.subList(0, cutFromStartIndex).clear();
        }

        List<AppointmentInformationEntity> appointmentInformationEntityList = appointmentInformationRepository
                .getAppointmentTimesForDateRange(dateRange.getStartTimestamp(), dateRange.getEndTimestamp());
        if(appointmentInformationEntityList != null) {
            for(AppointmentInformationEntity appointmentInformationEntity : appointmentInformationEntityList) {
                String strTimestamp = appointmentInformationEntity.getAppointmentTimestamp()
                        .toString().substring(0, 16).replace("T", " ") + ":00";
                int listSize = timeList.size();
                int i = 0;
                do {
                    if(listSize == 0) {
                        break;
                    }
                    String strApptTs = timeList.get(i).getAppointmentTimestamp();
                    if(strApptTs.equals(strTimestamp) || TimestampUtil.isValidTimestamp(TimestampUtil.getOffsetDateTime(strApptTs))) {
                        timeList.remove(i);
                        listSize--;
                        i--;
                    }
                    i++;
                } while(i < listSize);
            }
        }
        return timeList;
    }

    /**
     * Check appointment if it is duplicate
     * @param appointmentRequest appointmentRequest
     * @return AppointmentResponse
     */
    private AppointmentResponse checkDuplicateAppointment(AppointmentRequest appointmentRequest) {
        String serviceCode = ServiceType.getServiceCode(appointmentRequest.getServiceName());
        AppointmentInformationEntity appointmentInformationEntity = appointmentInformationRepository.checkDuplicateAppointment
                (appointmentRequest.getCustomerFirstName(),
                 appointmentRequest.getCustomerLastName(),
                 serviceCode);

        AppointmentResponse appointmentResponse = null;
        if(appointmentInformationEntity != null) {
            int appointmentStatusCode = appointmentInformationEntity.getAppointmentStatusCode();
            String bciReasonCode = appointmentInformationEntity.getBciReasonCode();
            String fbiReasonCode = appointmentInformationEntity.getFbiReasonCode();
            appointmentResponse = AppointmentResponse.builder()
                    .appointmentId(appointmentInformationEntity.getAppointmentId())
                    .orderTimestamp(appointmentInformationEntity.getOrderTimestamp())
                    .serviceName(ServiceType.getServiceName(serviceCode))
                    .bciReasonCode(bciReasonCode)
                    .bciReasonText(ReasonService.getReasonText(serviceCode, bciReasonCode))
                    .fbiReasonCode(fbiReasonCode)
                    .fbiReasonText(ReasonService.getReasonText(serviceCode, fbiReasonCode))
                    .appointmentTimestamp(appointmentInformationEntity.getAppointmentTimestamp())
                    .appointmentStatus(AppointmentStatus.getStatusName(appointmentStatusCode))
                    .statusTimestamp(appointmentStatusCode == 101 ? appointmentInformationEntity.getOrderTimestamp() : appointmentInformationEntity.getResheduleTimestamp())
                    .customerFirstName(appointmentInformationEntity.getCustomerFirstName())
                    .customerLastName(appointmentInformationEntity.getCustomerLastName())
                    .customerEmail(appointmentInformationEntity.getCustomerEmail())
                    .customerPhone(appointmentInformationEntity.getCustomerPhone())
                    .build();
        }

        return appointmentResponse;
    }

    /**
     * Validate appointment status as a final state
     * @param appointmentStatusCode appointmentStatusCode
     * @return TRUE/FALSE
     */
    private boolean isAppointmentStatusFinal(int appointmentStatusCode) {
        return appointmentStatusCode == AppointmentStatus.Cancelled.getStatusCode()
               || appointmentStatusCode == AppointmentStatus.Completed.getStatusCode();
    }

    /**
     * Assign reason text values to save in appt_info table when reasonCode is NO ORC or null
     * @param bciReasonCode bciReasonCode
     * @param bciReasonText bciReasonText
     * @param fbiReasonCode fbiReasonCode
     * @param fbiReasonText fbiReasonText
     * @return ReasonText
     */
    private ReasonText getReasonText(String bciReasonCode, String bciReasonText, String fbiReasonCode, String fbiReasonText) {
        return ReasonText.builder()
                .bciReasonText((bciReasonCode != null && bciReasonCode.equals("NO ORC")) || (bciReasonCode == null && bciReasonText != null) ? bciReasonText : null)
                .fbiReasonText((fbiReasonCode != null && fbiReasonCode.equals("NO ORC")) || (fbiReasonCode == null && fbiReasonText != null) ? fbiReasonText : null)
                .build();
    }

}
