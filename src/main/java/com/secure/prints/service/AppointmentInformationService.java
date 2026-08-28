package com.secure.prints.service;

import com.secure.prints.config.RequiresLogin;
import com.secure.prints.database.AppointmentPaymentRepository;
import com.secure.prints.database.entity.AppointmentInformationEntity;
import com.secure.prints.database.entity.AppointmentPaymentEntity;
import com.secure.prints.database.entity.ExpenseEntity;
import com.secure.prints.model.*;
import com.secure.prints.database.AppointmentInformationRepository;
import com.secure.prints.util.NameFormatUtil;
import com.secure.prints.util.TimestampUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
//@Transactional
public class AppointmentInformationService {

    private final AppointmentInformationRepository appointmentInformationRepository;
    private final AppointmentPaymentRepository appointmentPaymentRepository;
    private final AwsService awsService;
    private final ExpenseService expenseService;
    private int responseCode;
    private String responseMessage;
    @Value("${secure-prints.appointment.cut-from-start-index}")
    private int cutFromStartIndex;
    @Value("${secure-prints.appointment.cut-from-end-index}")
    private int cutFromEndIndex;
    @Value("${secure-prints.appointment.start-work-hour}")
    private int startWorkHour;
    @Value("${secure-prints.appointment.end-work-hour}")
    private int endWorkHour;
    @Value("${secure-prints.appointment.start-break-hour}")
    private int startBreakHour;
    @Value("${secure-prints.appointment.end-break-hour}")
    private int endBreakHour;

    /**
     * Constructor for AppointmentInformationService
     * @param appointmentInformationRepository appointmentInformationRepository
     * @param appointmentPaymentRepository appointmentPaymentRepository
     * @param expenseService expenseService
     * @param awsService awsService
     */
    public AppointmentInformationService(AppointmentInformationRepository appointmentInformationRepository,
                                         AppointmentPaymentRepository appointmentPaymentRepository,
                                         ExpenseService expenseService, AwsService awsService) {
        this.appointmentInformationRepository = appointmentInformationRepository;
        this.appointmentPaymentRepository = appointmentPaymentRepository;
        this.expenseService = expenseService;
        this.awsService = awsService;
    }

    /**
     * Schedule Appointment
     * @param appointmentRequest appointmentRequest
     * @return ApiResponse
     */
    public ApiResponse scheduleAppointment(AppointmentRequest appointmentRequest) {
        responseCode = 409;
        OffsetDateTime currentTimestamp = OffsetDateTime.now();
        OffsetDateTime appointmentTimestamp = TimestampUtil.getOffsetDateTime(appointmentRequest.getAppointmentTimestamp());
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if(UserService.isUserLoggedIn(request) && appointmentTimestamp.toLocalDate().isBefore(currentTimestamp.toLocalDate())) {
            currentTimestamp = appointmentTimestamp;
        }
        AppointmentResponse appointmentResponse = this.checkDuplicateAppointment(appointmentRequest);

        if(appointmentResponse != null) {
            responseMessage = "Duplicate appointment for the same service was found and not processed yet.";
        } else {
            String serviceName = appointmentRequest.getServiceName();
            String serviceCode = ServiceType.getServiceCode(serviceName);
            String bciReasonCode = appointmentRequest.getBciReasonCode();
            String fbiReasonCode = appointmentRequest.getFbiReasonCode();
            String code;
            String bciReasonDescription = null;
            String fbiReasonDescription = null;
            assert serviceCode != null;
            if (serviceCode.equals(ServiceType.BCI.name()) || serviceCode.equals(ServiceType.BCI_FBI.name())) {
                code = ReasonService.getReasonCode(ServiceType.BCI.name(), appointmentRequest.getBciReasonDescription());
                bciReasonCode = code != null ? code : "NO ORC";
                bciReasonDescription = bciReasonCode.equals("NO ORC") ? appointmentRequest.getBciReasonDescription() : null;
            }
            if (serviceCode.equals(ServiceType.FBI.name()) || serviceCode.equals(ServiceType.BCI_FBI.name())) {
                code = ReasonService.getReasonCode(ServiceType.FBI.name(), appointmentRequest.getFbiReasonDescription());
                fbiReasonCode = code != null ? code : "NO ORC";
                fbiReasonDescription = fbiReasonCode.equals("NO ORC") ? appointmentRequest.getFbiReasonDescription() : null;
            }

            // Get next sequence value for appt_info table primary key
            String appointmentId = String.valueOf(appointmentInformationRepository.getNextAppointmentId());
            assert bciReasonCode != null;
            assert fbiReasonCode != null;
            String customerEmail = appointmentRequest.getCustomerEmail().toLowerCase();
            AppointmentInformationEntity appointmentInformationEntity = AppointmentInformationEntity.builder()
                    .appointmentId(appointmentId)
                    .customerFirstName(NameFormatUtil.formatName(appointmentRequest.getCustomerFirstName()))
                    .customerLastName(NameFormatUtil.formatName(appointmentRequest.getCustomerLastName()))
                    .customerEmail(customerEmail)
                    .customerPhone(appointmentRequest.getCustomerPhone())
                    .serviceCode(serviceCode)
                    .bciReasonCode(bciReasonCode)
                    .bciReasonDescription(bciReasonDescription)
                    .fbiReasonCode(fbiReasonCode)
                    .fbiReasonDescription(fbiReasonDescription)
                    .appointmentTimestamp(appointmentTimestamp)
                    .appointmentStatusCode(AppointmentStatus.Scheduled.getStatusCode())
                    .orderTimestamp(currentTimestamp)
                    .userIpAddress(this.getUserIpAddress(appointmentRequest.getUserName()))
                    .build();
            try {
                appointmentInformationRepository.saveAndFlush(appointmentInformationEntity);
            } catch (DataIntegrityViolationException ex) {
                responseMessage = "This appointment time was just booked. Please select another time.";
                ApiStatus apiStatus = ApiStatus.builder()
                        .responseCode(responseCode)
                        .responseMessage(responseMessage)
                        .build();
                return ApiResponse.builder()
                        .apiStatus(apiStatus)
                        .apiResponseEntity(null)
                        .build();
            }

            // Add payment entry to appt_pymt table
            AppointmentPaymentEntity appointmentPaymentEntity = AppointmentPaymentEntity.builder()
                    .appointmentId(appointmentId)
                    .serviceAmount(ServiceType.getServiceFee(serviceCode, appointmentTimestamp.toLocalDate()))
                    .bciAmount(ServiceType.getBciFee(serviceCode, appointmentTimestamp.toLocalDate()))
                    .paymentStatusCode(PaymentStatus.Pending.getPaymentStatusCode())
                    .paymentDate(LocalDate.from(appointmentTimestamp))
                    .paymentComment("")
                    .paymentUpdate(true)
                    .build();
            appointmentPaymentRepository.save(appointmentPaymentEntity);

            appointmentResponse = AppointmentResponse.builder()
                    .appointmentId(appointmentId)
                    .orderTimestamp(TimestampUtil.formatTimestamp(appointmentInformationEntity.getOrderTimestamp()))
                    .serviceName(serviceName)
                    .bciReasonCode(bciReasonCode)
                    .bciReasonDescription(appointmentRequest.getBciReasonDescription())
                    .fbiReasonCode(fbiReasonCode)
                    .fbiReasonDescription(appointmentRequest.getFbiReasonDescription())
                    .appointmentTimestamp(TimestampUtil.formatDateTime(appointmentInformationEntity.getAppointmentTimestamp()))
                    .appointmentStatus(AppointmentStatus.Scheduled.name())
                    .statusTimestamp(TimestampUtil.formatTimestamp(currentTimestamp))
                    .build();

            responseCode = 201;
            responseMessage = "Appointment Scheduled.";

            // Send confirmation email to customer
            try {
                String customerName = appointmentInformationEntity.getCustomerFirstName() + " " + appointmentInformationEntity.getCustomerLastName();
                awsService.sendConfirmationEmail(customerName, customerEmail, appointmentResponse);
            } catch (Exception ex) {
                responseCode = 409;
                responseMessage = "Appointment Scheduled. Sending confirmation email failed.";
            }

        }

        ApiStatus apiStatus = ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
        return ApiResponse.builder()
                .apiStatus(apiStatus)
                .apiResponseEntity(appointmentResponse)
                .build();
    }

    /**
     * Reschedule Appointment
     * @param appointmentId appointmentId
     * @param strAppointmentTimestamp strAppointmentTimestamp
     * @return ApiResponse
     */
    public ApiResponse rescheduleAppointment(String appointmentId, String strAppointmentTimestamp) {
        responseCode = 409;
        ApiStatus apiStatus;
        AppointmentResponse appointmentResponse;
        ApiResponse apiResponse;
        AppointmentInformationEntity appointmentInformationEntity = (AppointmentInformationEntity) this.getAppointmentDetails(appointmentId).getApiResponseEntity();
        int appointmentStatusCode = 0;
        if(appointmentInformationEntity != null) {
            appointmentStatusCode = appointmentInformationEntity.getAppointmentStatusCode();
        }

        OffsetDateTime currentTimestamp = OffsetDateTime.now();
        OffsetDateTime appointmentTimestamp = TimestampUtil.getOffsetDateTime(strAppointmentTimestamp);
        String strNewAppointmentTs = appointmentTimestamp.toString().substring(0, appointmentTimestamp.toString().length() - 6) + "Z";
        if(appointmentInformationEntity == null) {
            responseMessage = "Appointment ID not found.";
        } else if(this.isAppointmentStatusCancelledOrCompleted(appointmentStatusCode)) {
            responseMessage = "Invalid appointment status to reschedule. Current status: " + AppointmentStatus.getStatusName(appointmentStatusCode);
        } else if(appointmentInformationEntity.getAppointmentTimestamp().toString().equals(strNewAppointmentTs)
                && appointmentInformationEntity.getAppointmentStatusCode() != AppointmentStatus.Cancelled.getStatusCode()) {
            responseMessage = "The given appointment date and time are the same in our records (no change has been done).";
        } else {
            try {
                appointmentInformationRepository.rescheduleAppointment(appointmentId, appointmentTimestamp, currentTimestamp);
                appointmentPaymentRepository.updatePaymentStatusAndDate(appointmentId, PaymentStatus.Pending.getPaymentStatusCode(), appointmentTimestamp.toLocalDate());
                responseCode = 200;
                responseMessage = "Appointment Rescheduled.";
            } catch (DataIntegrityViolationException ex) {
                responseCode = 409;
                responseMessage = "This appointment time was just booked. Please select another time.";
                apiStatus = ApiStatus.builder()
                        .responseCode(responseCode)
                        .responseMessage(responseMessage)
                        .build();
                return ApiResponse.builder()
                        .apiStatus(apiStatus)
                        .apiResponseEntity(null)
                        .build();
            }
        }

        // Update payment entry to appt_pymt table
        assert appointmentInformationEntity != null;
        String serviceCode = appointmentInformationEntity.getServiceCode();
        AppointmentPaymentEntity appointmentPaymentEntity = AppointmentPaymentEntity.builder()
                .appointmentId(appointmentId)
                .serviceAmount(ServiceType.getServiceFee(serviceCode, appointmentTimestamp.toLocalDate()))
                .bciAmount(ServiceType.getBciFee(serviceCode, appointmentTimestamp.toLocalDate()))
                .paymentStatusCode(PaymentStatus.Pending.getPaymentStatusCode())
                .paymentDate(LocalDate.from(appointmentTimestamp))
                .paymentComment("")
                .paymentUpdate(true)
                .build();
        appointmentPaymentRepository.save(appointmentPaymentEntity);

        String bciReasonCode = appointmentInformationEntity.getBciReasonCode();
        String bciReasonDescription = bciReasonCode != null && bciReasonCode.equals("NO ORC") ? appointmentInformationEntity.getBciReasonDescription() : bciReasonCode != null ? ReasonService.getReasonDescription("BCI", bciReasonCode) : null;
        String fbiReasonCode = appointmentInformationEntity.getFbiReasonCode();
        String fbiReasonDescription = fbiReasonCode != null && fbiReasonCode.equals("NO ORC") ? appointmentInformationEntity.getFbiReasonDescription() : fbiReasonCode != null ? ReasonService.getReasonDescription("FBI", fbiReasonCode) : null;
        appointmentResponse = AppointmentResponse.builder()
                .appointmentId(appointmentId)
                .orderTimestamp(TimestampUtil.formatTimestamp(appointmentInformationEntity.getOrderTimestamp()))
                .serviceName(ServiceType.getServiceName(serviceCode))
                .bciReasonCode(appointmentInformationEntity.getBciReasonCode())
                .bciReasonDescription(bciReasonDescription)
                .fbiReasonCode(appointmentInformationEntity.getFbiReasonCode())
                .fbiReasonDescription(fbiReasonDescription)
                .appointmentTimestamp(TimestampUtil.formatDateTime(appointmentTimestamp))
                .appointmentStatus(AppointmentStatus.Rescheduled.name())
                .statusTimestamp(TimestampUtil.formatTimestamp(currentTimestamp))
                .build();

        // Send confirmation email to customer
        try {
            String customerName = appointmentInformationEntity.getCustomerFirstName() + " " + appointmentInformationEntity.getCustomerLastName();
            awsService.sendConfirmationEmail(customerName, appointmentInformationEntity.getCustomerEmail(), appointmentResponse);
        } catch (Exception ex) {
            responseCode = 409;
            responseMessage = "Appointment Rescheduled. Sending confirmation email failed.";
        }

        apiStatus = ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
        apiResponse = ApiResponse.builder()
                .apiStatus(apiStatus)
                .apiResponseEntity(appointmentResponse)
                .build();
        return apiResponse;
    }

    /**
     * Cancel Appointment
     * @param appointmentId appointmentId
     * @return ApiResponse
     */
    public ApiResponse cancelAppointment(String appointmentId) {
        responseCode = 409;
        ApiStatus apiStatus;
        AppointmentResponse appointmentResponse;
        ApiResponse apiResponse;
        AppointmentInformationEntity appointmentInformationEntity = (AppointmentInformationEntity) this.getAppointmentDetails(appointmentId).getApiResponseEntity();
        int appointmentStatusCode = 0;
        if(appointmentInformationEntity != null) {
            appointmentStatusCode = appointmentInformationEntity.getAppointmentStatusCode();
        }

        OffsetDateTime currentTimestamp = OffsetDateTime.now();
        if(appointmentInformationEntity == null) {
            responseMessage = "Appointment ID not found.";
        } else if(this.isAppointmentStatusCancelledOrCompleted(appointmentStatusCode)) {
            responseMessage = "Invalid appointment status to cancel. Current status: " + AppointmentStatus.getStatusName(appointmentStatusCode);
        } else {
            appointmentInformationRepository.cancelAppointment(appointmentId, currentTimestamp);
            appointmentPaymentRepository.updatePaymentStatusAndDate(appointmentId, PaymentStatus.Cancelled.getPaymentStatusCode(), LocalDate.now());
            responseCode = 200;
            responseMessage = "Appointment Cancelled.";
        }

        String serviceCode = appointmentInformationEntity.getServiceCode();
        String bciReasonCode = appointmentInformationEntity.getBciReasonCode();
        String bciReasonDescription = bciReasonCode != null && bciReasonCode.equals("NO ORC") ? appointmentInformationEntity.getBciReasonDescription() : bciReasonCode != null ? ReasonService.getReasonDescription("BCI", bciReasonCode) : null;
        String fbiReasonCode = appointmentInformationEntity.getFbiReasonCode();
        String fbiReasonDescription = fbiReasonCode != null && fbiReasonCode.equals("NO ORC") ? appointmentInformationEntity.getFbiReasonDescription() : fbiReasonCode != null ? ReasonService.getReasonDescription("FBI", fbiReasonCode) : null;
        appointmentResponse = AppointmentResponse.builder()
                .appointmentId(appointmentId)
                .orderTimestamp(TimestampUtil.formatTimestamp(appointmentInformationEntity.getOrderTimestamp()))
                .serviceName(ServiceType.getServiceName(serviceCode))
                .bciReasonCode(appointmentInformationEntity.getBciReasonCode())
                .bciReasonDescription(bciReasonDescription)
                .fbiReasonCode(appointmentInformationEntity.getFbiReasonCode())
                .fbiReasonDescription(fbiReasonDescription)
                .appointmentTimestamp(TimestampUtil.formatDateTime(appointmentInformationEntity.getAppointmentTimestamp()))
                .appointmentStatus(AppointmentStatus.Cancelled.name())
                .statusTimestamp(TimestampUtil.formatTimestamp(currentTimestamp))
                .build();

        // Send confirmation email to customer
        try {
            String customerName = appointmentInformationEntity.getCustomerFirstName() + " " + appointmentInformationEntity.getCustomerLastName();
            awsService.sendConfirmationEmail(customerName, appointmentInformationEntity.getCustomerEmail(), appointmentResponse);
        } catch (Exception ex) {
            responseCode = 409;
            responseMessage = "Appointment Cancelled. Sending confirmation email failed.";
        }

        apiStatus = ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
        apiResponse = ApiResponse.builder()
                .apiStatus(apiStatus)
                .apiResponseEntity(appointmentResponse)
                .build();
        return apiResponse;
    }

    /**
     * Update appointment status to Completed and add payment entry
     * @param appointmentId appointmentId
     * @param paymentMethodName paymentMethodName
     * @return ApiResponse
     */
    @RequiresLogin
    public ApiResponse completeAppointment(String appointmentId, String paymentMethodName) {
        responseCode = 409;
        ApiStatus apiStatus;
        AppointmentResponse appointmentResponse;
        ApiResponse apiResponse;
        AppointmentInformationEntity appointmentInformationEntity = (AppointmentInformationEntity) this.getAppointmentDetails(appointmentId).getApiResponseEntity();
        int appointmentStatusCode = 0;
        if(appointmentInformationEntity != null) {
            appointmentStatusCode = appointmentInformationEntity.getAppointmentStatusCode();
        }

        OffsetDateTime currentTimestamp = OffsetDateTime.now();
        OffsetDateTime completeTimestamp = currentTimestamp;
        if(appointmentInformationEntity == null) {
            responseMessage = "Appointment ID not found.";
        } else if(this.isAppointmentStatusCancelledOrCompleted(appointmentStatusCode)) {
            responseMessage = "Invalid appointment status to complete. Current status: " + AppointmentStatus.getStatusName(appointmentStatusCode);
        } else if(currentTimestamp.isBefore(appointmentInformationEntity.getAppointmentTimestamp())) {
            responseMessage = "Change appointment status to 'Completed' is not allowed before appointment date.";
        } else {
            String strTimestamp = appointmentInformationEntity.getAppointmentTimestamp().plusMinutes(15).toString();
            completeTimestamp = TimestampUtil.getOffsetDateTime(strTimestamp.substring(0, 10) + " " + strTimestamp.substring(11, 16) + ":00");
            appointmentInformationRepository.completeAppointment(appointmentId, completeTimestamp);
            BigDecimal transactionFees = BigDecimal.ZERO;
            LocalDate completeDate = completeTimestamp.toLocalDate();
            int paymentMethodCode = PaymentMethod.getPaymentMethodCode(paymentMethodName);
            if(paymentMethodName.equals(PaymentMethod.Card.name())) {
                AppointmentPaymentEntity appointmentPaymentEntity = appointmentPaymentRepository.findPaymentByAppointmentId(appointmentId);
                transactionFees = appointmentPaymentEntity.getServiceAmount().multiply(BigDecimal.valueOf(0.026)).add(BigDecimal.valueOf(0.15));

                // Add transactionFees value to Expense table as expense subcategory of 604
                expenseService.addExpenseDetails(ExpenseEntity.builder()
                                .expenseVendorName("Square (CC Reader)")
                                .expenseReferenceNumber("ApptID-" + appointmentId)
                                .expenseReferenceDate(completeDate)
                                .expenseDescription("CC Reader fee.")
                                .expenseCategoryCode(600)
                                .expenseSubcategoryCode(604)
                                .expenseAmount(transactionFees)
                                .expensePaymentStatusCode(202)
                                .expensePaymentMethodCode(paymentMethodCode)
                                .expensePaymentDate(completeDate)
                                .build());
            }
            appointmentPaymentRepository.updatePaymentStatusAndMethod(appointmentId, PaymentStatus.Processed.getPaymentStatusCode(),
                    paymentMethodCode, transactionFees);
            responseCode = 200;
            responseMessage = "Appointment Completed.";
        }

        String serviceCode = appointmentInformationEntity.getServiceCode();
        String bciReasonCode = appointmentInformationEntity.getBciReasonCode();
        String bciReasonDescription = bciReasonCode != null && bciReasonCode.equals("NO ORC") ? appointmentInformationEntity.getBciReasonDescription() : bciReasonCode != null ? ReasonService.getReasonDescription("BCI", bciReasonCode) : null;
        String fbiReasonCode = appointmentInformationEntity.getFbiReasonCode();
        String fbiReasonDescription = fbiReasonCode != null && fbiReasonCode.equals("NO ORC") ? appointmentInformationEntity.getFbiReasonDescription() : fbiReasonCode != null ? ReasonService.getReasonDescription("FBI", fbiReasonCode) : null;
        appointmentResponse = AppointmentResponse.builder()
                .appointmentId(appointmentId)
                .orderTimestamp(TimestampUtil.formatTimestamp(appointmentInformationEntity.getOrderTimestamp()))
                .serviceName(ServiceType.getServiceName(serviceCode))
                .bciReasonCode(appointmentInformationEntity.getBciReasonCode())
                .bciReasonDescription(bciReasonDescription)
                .fbiReasonCode(appointmentInformationEntity.getFbiReasonCode())
                .fbiReasonDescription(fbiReasonDescription)
                .appointmentTimestamp(TimestampUtil.formatDateTime(appointmentInformationEntity.getAppointmentTimestamp()))
                .appointmentStatus(AppointmentStatus.Completed.name())
                .statusTimestamp(TimestampUtil.formatTimestamp(completeTimestamp))
                .build();

        // Send confirmation email to customer
        try {
            String customerName = appointmentInformationEntity.getCustomerFirstName() + " " + appointmentInformationEntity.getCustomerLastName();
            awsService.sendConfirmationEmail(customerName, appointmentInformationEntity.getCustomerEmail(), appointmentResponse);
        } catch (Exception ex) {
            responseCode = 409;
            responseMessage = "Appointment Completed. Sending confirmation email failed.";
        }

        apiStatus = ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
        apiResponse = ApiResponse.builder()
                .apiStatus(apiStatus)
                .apiResponseEntity(appointmentResponse)
                .build();
        return apiResponse;
    }

    /**
     * Get appointment details by appointment ID
     * @param appointmentId appointmentId
     * @return ApiResponse
     */
    @RequiresLogin
    public ApiResponse getAppointmentDetails(String appointmentId) {
        AppointmentInformationEntity appointment = appointmentInformationRepository.findByAppointmentId(appointmentId);
        responseCode = appointment != null ? 200 : 409;
        responseMessage = appointment != null ? "Appointment details retrieved." : "Appointment ID not found.";
        return ApiResponse.builder()
                .apiStatus(new ApiStatus(responseCode, responseMessage))
                .apiResponseEntity(appointment)
                .build();
    }

    /**
     * Find appointment by appointment ID
     * @param appointmentId appointmentId
     * @return TRUE = Found / FALSE = Not Found
     */
    public boolean findAppointmentById(String appointmentId) {
        AppointmentInformationEntity appointment = appointmentInformationRepository.findByAppointmentId(appointmentId);
        return !(appointment == null || this.isAppointmentStatusCancelledOrCompleted(appointment.getAppointmentStatusCode()));
    }

    /**
     * Find appointment by customer first and last name
     * @param customerFirstName customerFirstName
     * @param customerLastName customerLastName
     * @return appointmentId
     */
    public String findAppointmentByCustomerName(String customerFirstName, String customerLastName) {
        AppointmentInformationEntity appointment = appointmentInformationRepository.findAppointmentByCustomerName(
                NameFormatUtil.formatName(customerFirstName), NameFormatUtil.formatName(customerLastName));
        return appointment != null ? appointment.getAppointmentId() : null;
    }

    /**
     * Get list of all appointments or appointments for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @return List of appointments
     */
    @RequiresLogin
    public Appointment getAllAppointments(LocalDate startDate, LocalDate endDate) {
        List<AppointmentInformationEntity> appointmentList = new ArrayList<>();
        List<AppointmentResponse> responseList = new ArrayList<>();

        // Get Appointment Entity list
        DateRange dateRange = null;
        if(startDate != null && endDate != null) {
            dateRange = TimestampUtil.getOffsetDateRange(startDate, endDate);
        } else if(startDate == null && endDate == null) {
            String strStartDate = LocalDate.now().getYear() + "-01-01";
            String strEndDate = LocalDate.now().getYear() + "-12-31";
            dateRange = TimestampUtil.getOffsetDateRange(LocalDate.parse(strStartDate), LocalDate.parse(strEndDate));
        }
        assert dateRange != null;
        appointmentList = appointmentInformationRepository.getAllAppointmentsForDateRange(dateRange.getStartTimestamp(), dateRange.getEndTimestamp());

        // Generate Appointment Response list
        assert appointmentList != null;
        if(!appointmentList.isEmpty()) {
            appointmentList.forEach(appointment -> {
                String bciReasonCode = appointment.getBciReasonCode();
                String bciReasonDescription = bciReasonCode != null && bciReasonCode.equals("NO ORC") ? appointment.getBciReasonDescription() : bciReasonCode != null ? ReasonService.getReasonDescription("BCI", bciReasonCode) : null;
                String fbiReasonCode = appointment.getFbiReasonCode();
                String fbiReasonDescription = fbiReasonCode != null && fbiReasonCode.equals("NO ORC") ? appointment.getFbiReasonDescription() : fbiReasonCode != null ? ReasonService.getReasonDescription("FBI", fbiReasonCode) : null;
                int appointmentStatusCode = appointment.getAppointmentStatusCode();
                OffsetDateTime  statusTimestamp = OffsetDateTime.now();
                switch(appointmentStatusCode) {
                    case 101: statusTimestamp = appointment.getOrderTimestamp(); break;
                    case 102: statusTimestamp = appointment.getResheduleTimestamp(); break;
                    case 103: statusTimestamp = appointment.getCancelTimestamp(); break;
                    case 104: statusTimestamp = appointment.getCompleteTimestamp();
                };
                OffsetDateTime currentTimestamp = OffsetDateTime.now();
                String apptStrTimestamp = appointment.getAppointmentTimestamp().toString();
                String offsetStrTimestamp = apptStrTimestamp.substring(0, 10) + " " + apptStrTimestamp.substring(11, 16) + ":00";
                List<String> statusHistory = new ArrayList<>();
                if(appointment.getOrderTimestamp() != null) {
                    statusHistory.add("Scheduled: " + TimestampUtil.formatTimestamp(appointment.getOrderTimestamp()));
                }
                if(appointment.getResheduleTimestamp() != null) {
                    statusHistory.add("Rescheduled: " + TimestampUtil.formatTimestamp(appointment.getResheduleTimestamp()));
                }
                if(appointment.getCancelTimestamp() != null) {
                    statusHistory.add("Cancelled: " + TimestampUtil.formatTimestamp(appointment.getCancelTimestamp()));
                }
                if(appointment.getCompleteTimestamp() != null) {
                    statusHistory.add("Completed: " + TimestampUtil.formatTimestamp(appointment.getCompleteTimestamp()));
                }
                AppointmentResponse appointmentResponse = AppointmentResponse.builder()
                        .appointmentId(appointment.getAppointmentId())
                        .orderTimestamp(TimestampUtil.formatTimestamp(appointment.getOrderTimestamp()))
                        .serviceName(ServiceType.getServiceName(appointment.getServiceCode()))
                        .bciReasonCode(appointment.getBciReasonCode())
                        .bciReasonDescription(bciReasonDescription)
                        .fbiReasonCode(appointment.getFbiReasonCode())
                        .fbiReasonDescription(fbiReasonDescription)
                        .appointmentTimestamp(TimestampUtil.formatDateTime(appointment.getAppointmentStatusCode() != 103 ?
                                appointment.getAppointmentTimestamp() : appointment.getCancelledAppointmentTimestamp()))
                        .appointmentStatus(AppointmentStatus.getStatusName(appointmentStatusCode))
                        .statusTimestamp(TimestampUtil.formatTimestamp(statusTimestamp))
                        .statusHistory(statusHistory)
                        .canComplete(currentTimestamp.isAfter(TimestampUtil.getOffsetDateTime(offsetStrTimestamp)))
                        .build();
                responseList.add(appointmentResponse);
            });
        }
        return new Appointment(appointmentList, responseList);
    }

    /**
     * Generate all available appointments list for a specific date
     * @param selectedDate selectedDate
     * @return List of available appointments
     */
    public List<AppointmentTime> generateAppointmentTimes(LocalDate selectedDate) {
        List<AppointmentTime> timeList = new ArrayList<>();
        LocalDate companyHolidayStartDate = CompanyService.getCompanyDetails(1).getCompanyHolidayStartDate();
        LocalDate companyHolidayEndDate = CompanyService.getCompanyDetails(1).getCompanyHolidayEndDate();
        if(companyHolidayStartDate != null && !selectedDate.isBefore(companyHolidayStartDate)
                && companyHolidayEndDate != null && !selectedDate.isAfter(companyHolidayEndDate)) {
            return timeList;
        }

        String strDate = selectedDate.toString() + " ";
        DateRange dateRange = TimestampUtil.getOffsetDateRange(selectedDate, selectedDate);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if(!UserService.isUserLoggedIn(request) && TimestampUtil.isValidTimestamp(dateRange.getEndTimestamp())) {
            return timeList;
        }

        // Create Time List
        StringBuilder timeLabel;
        StringBuilder apptTs;
        for(int hour = startWorkHour; hour <= endWorkHour - 1; hour++) {
            // Delete appointments from middle of the list for a break time
            if((startBreakHour > 0 && endBreakHour > 0) && (hour >= startBreakHour && hour <= endBreakHour)) {
                continue;
            }
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

        // Delete appointments from top of the list
        if(cutFromStartIndex > 0) {
            timeList.subList(0, cutFromStartIndex).clear();
        }

        // Delete appointments from bottom of the list
        if(cutFromEndIndex > 0) {
            timeList.subList(cutFromEndIndex, timeList.size()).clear();
        }

        int listSize = timeList.size();
        int i = 0;
        List<AppointmentInformationEntity> appointmentInformationEntityList = appointmentInformationRepository
                .getActiveAppointmentTimesForDateRange(dateRange.getStartTimestamp(), dateRange.getEndTimestamp());
        if(UserService.isUserLoggedIn(request) || !appointmentInformationEntityList.isEmpty()) {
            // Remove booked appointments from the list
//            for(AppointmentInformationEntity appointmentInformationEntity : appointmentInformationEntityList) {
//                String strTimestamp = appointmentInformationEntity.getAppointmentTimestamp()
//                        .toString().substring(0, 16).replace("T", " ") + ":00";
//                do {
//                    if(listSize == 0) {
//                        break;
//                    }
//                    String strApptTs = timeList.get(i).getAppointmentTimestamp();
//                    if(strApptTs.equals(strTimestamp) || (!UserService.isUserLoggedIn(request) && TimestampUtil.isValidTimestamp(TimestampUtil.getOffsetDateTime(strApptTs)))) {
//                        timeList.remove(i);
//                        listSize--;
//                        i--;
//                        break;
//                    }
//                    i++;
//                } while(i < listSize);
//            }
        } else {
            // Remove appointments of holiday days from the list
            do {
                if(listSize == 0) {
                    break;
                }
                OffsetDateTime appointmentTs = TimestampUtil.getOffsetDateTime(timeList.get(i).getAppointmentTimestamp());
                if(TimestampUtil.isValidTimestamp(appointmentTs)) {
                    timeList.remove(i);
                    listSize--;
                    i--;
                }
                i++;
            } while(i < listSize);
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
                (NameFormatUtil.formatName(appointmentRequest.getCustomerFirstName()),
                 NameFormatUtil.formatName(appointmentRequest.getCustomerLastName()),
                 serviceCode);

        AppointmentResponse appointmentResponse = null;
        if(appointmentInformationEntity != null) {
            int appointmentStatusCode = appointmentInformationEntity.getAppointmentStatusCode();
            String bciReasonCode = appointmentInformationEntity.getBciReasonCode();
            String fbiReasonCode = appointmentInformationEntity.getFbiReasonCode();
            appointmentResponse = AppointmentResponse.builder()
                    .appointmentId(appointmentInformationEntity.getAppointmentId())
                    .orderTimestamp(TimestampUtil.formatTimestamp(appointmentInformationEntity.getOrderTimestamp()))
                    .serviceName(ServiceType.getServiceName(serviceCode))
                    .bciReasonCode(bciReasonCode)
                    .bciReasonDescription(bciReasonCode != null ? ReasonService.getReasonDescription("BCI", bciReasonCode) :
                            appointmentInformationEntity.getBciReasonDescription())
                    .fbiReasonCode(fbiReasonCode)
                    .fbiReasonDescription(fbiReasonCode != null ? ReasonService.getReasonDescription("FBI", fbiReasonCode) :
                            appointmentInformationEntity.getFbiReasonDescription())
                    .appointmentTimestamp(TimestampUtil.formatDateTime(appointmentInformationEntity.getAppointmentTimestamp()))
                    .appointmentStatus(AppointmentStatus.getStatusName(appointmentStatusCode))
                    .statusTimestamp(appointmentStatusCode == 101 ? TimestampUtil.formatTimestamp(appointmentInformationEntity.getOrderTimestamp()) :
                            appointmentStatusCode == 102 ? TimestampUtil.formatTimestamp(appointmentInformationEntity.getResheduleTimestamp()) :
                                    TimestampUtil.formatTimestamp(appointmentInformationEntity.getCancelTimestamp()))
                    .build();
        }

        return appointmentResponse;
    }

    /**
     * Validate appointment status if it is Completed
     * @param appointmentStatusCode appointmentStatusCode
     * @return TRUE/FALSE
     */
    private boolean isAppointmentStatusCompleted(int appointmentStatusCode) {
        return appointmentStatusCode == AppointmentStatus.Completed.getStatusCode();
    }

    /**
     * Validate appointment status if it is Cancelled or Completed
     * @param appointmentStatusCode appointmentStatusCode
     * @return TRUE/FALSE
     */
    private boolean isAppointmentStatusCancelledOrCompleted(int appointmentStatusCode) {
        return appointmentStatusCode == AppointmentStatus.Cancelled.getStatusCode()
                || appointmentStatusCode == AppointmentStatus.Completed.getStatusCode();
    }

    /**
     * Gets Username or Remote IP Address
     * @return Username or Remote IP Address
     */
    private String getUserIpAddress(String userName) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String userIpAddress;
        if(userName == null) {
            userIpAddress = request.getHeader("X-Forwarded-For");
            if (userIpAddress == null || userIpAddress.isEmpty() || userIpAddress.equalsIgnoreCase("unknown")) {
                userIpAddress = request.getRemoteAddr();
            }
        } else {
            userIpAddress = userName;
        }
        return userIpAddress;
    }

}
