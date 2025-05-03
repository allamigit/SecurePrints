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
    @Value("${secure-prints.appointment.cut-from-index}")
    private int cutFromIndex;

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
        OffsetDateTime appointmentTimestamp = TimestampUtil.getOffsetTimestamp(appointmentRequest.getAppointmentDate(), appointmentRequest.getAppointmentTime());
        AppointmentResponse appointmentResponse = null;

        if(TimestampUtil.isValidTimestamp(appointmentTimestamp)) {
            responseCode = 409;
            responseMessage = "Invalid given appointment date or time (past date or weekend)";
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
     * @param strAppointmentDate strAppointmentDate
     * @param strAppointmentTime strAppointmentTime
     * @return ApiResponse
     */
    public ApiResponse rescheduleAppointment(long appointmentId, String strAppointmentDate, String strAppointmentTime) {
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
        OffsetDateTime appointmentTimestamp = TimestampUtil.getOffsetTimestamp(strAppointmentDate, strAppointmentTime);
        if(appointmentInformationEntity == null) {
            responseMessage = "Appointment ID not found";
        } else if(this.isAppointmentStatusFinal(appointmentStatusCode)) {
            responseMessage = "Invalid appointment status to reschedule: " + AppointmentStatus.getStatusName(appointmentStatusCode);
        } else if(TimestampUtil.isValidTimestamp(appointmentTimestamp)) {
            responseMessage = "Invalid given appointment date or time (past date or weekend)";
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
                    .appointmentId(appointmentId)
                    .serviceCode(serviceCode)
                    .serviceAmount(appointmentInformationEntity.getServiceAmount())
                    .paymentType(PaymentType.Fee.getPaymentTypeCode())
                    .paymentMethod(PaymentMethod.getPaymentMethodCode(paymentMethodName))
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
//        timeList.add(new AppointmentTime(strDate + "09:00:00", "9:00 AM"));
//        timeList.add(new AppointmentTime(strDate + "09:10:00", "9:10 AM"));
//        timeList.add(new AppointmentTime(strDate + "09:20:00", "9:20 AM"));
//        timeList.add(new AppointmentTime(strDate + "09:30:00", "9:30 AM"));
//        timeList.add(new AppointmentTime(strDate + "09:40:00", "9:40 AM"));
//        timeList.add(new AppointmentTime(strDate + "09:50:00", "9:50 AM"));
//        timeList.add(new AppointmentTime(strDate + "10:00:00", "10:00 AM"));
//        timeList.add(new AppointmentTime(strDate + "10:10:00", "10:10 AM"));
//        timeList.add(new AppointmentTime(strDate + "10:20:00", "10:20 AM"));
//        timeList.add(new AppointmentTime(strDate + "10:30:00", "10:30 AM"));
//        timeList.add(new AppointmentTime(strDate + "10:40:00", "10:40 AM"));
//        timeList.add(new AppointmentTime(strDate + "10:50:00", "10:50 AM"));
//        timeList.add(new AppointmentTime(strDate + "11:00:00", "11:00 AM"));
//        timeList.add(new AppointmentTime(strDate + "11:10:00", "11:10 AM"));
//        timeList.add(new AppointmentTime(strDate + "11:20:00", "11:20 AM"));
//        timeList.add(new AppointmentTime(strDate + "11:30:00", "11:30 AM"));
//        timeList.add(new AppointmentTime(strDate + "11:40:00", "11:40 AM"));
//        timeList.add(new AppointmentTime(strDate + "11:50:00", "11:50 AM"));
//        timeList.add(new AppointmentTime(strDate + "12:00:00", "12:00 PM"));
//        timeList.add(new AppointmentTime(strDate + "12:10:00", "12:10 PM"));
//        timeList.add(new AppointmentTime(strDate + "12:20:00", "12:20 PM"));
//        timeList.add(new AppointmentTime(strDate + "12:30:00", "12:30 PM"));
//        timeList.add(new AppointmentTime(strDate + "12:40:00", "12:40 PM"));
//        timeList.add(new AppointmentTime(strDate + "12:50:00", "12:50 PM"));
//        timeList.add(new AppointmentTime(strDate + "13:00:00", "1:00 PM"));
//        timeList.add(new AppointmentTime(strDate + "13:10:00", "1:10 PM"));
//        timeList.add(new AppointmentTime(strDate + "13:20:00", "1:20 PM"));
//        timeList.add(new AppointmentTime(strDate + "13:30:00", "1:30 PM"));
//        timeList.add(new AppointmentTime(strDate + "13:40:00", "1:40 PM"));
//        timeList.add(new AppointmentTime(strDate + "13:50:00", "1:50 PM"));

        DateRange dateRange = TimestampUtil.getOffsetDateRange(selectedDate, selectedDate);
        OffsetDateTime endTimestamp = dateRange.getEndTimestamp();
        if(OffsetDateTime.now().isAfter(endTimestamp) || endTimestamp.getDayOfWeek() == DayOfWeek.SATURDAY
                || endTimestamp.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return timeList;
        }

        timeList.add(new AppointmentTime(strDate + "13:40:00", "1:40 PM"));
        timeList.add(new AppointmentTime(strDate + "13:42:00", "1:42 PM"));
        timeList.add(new AppointmentTime(strDate + "13:44:00", "1:44 PM"));
        timeList.add(new AppointmentTime(strDate + "13:46:00", "1:46 PM"));
        timeList.add(new AppointmentTime(strDate + "13:48:00", "1:48 PM"));
        timeList.add(new AppointmentTime(strDate + "13:50:00", "1:50 PM"));

        if(dateRange.getStartTimestamp().getDayOfWeek() == DayOfWeek.FRIDAY) {
            timeList.subList(cutFromIndex, timeList.size()).clear();
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
