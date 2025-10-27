package com.secure.prints.service;

import com.secure.prints.config.RequiresLogin;
import com.secure.prints.database.AppointmentInformationRepository;
import com.secure.prints.database.AppointmentPaymentRepository;
import com.secure.prints.database.ExpenseRepository;
import com.secure.prints.database.entity.AppointmentInformationEntity;
import com.secure.prints.database.entity.AppointmentPaymentEntity;
import com.secure.prints.database.entity.ExpenseEntity;
import com.secure.prints.model.Payment;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.model.PaymentMethod;
import com.secure.prints.model.PaymentStatus;import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AppointmentPaymentService {

    private final AppointmentPaymentRepository appointmentPaymentRepository;
    private final AppointmentInformationRepository appointmentInformationRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;
    private int responseCode;
    private String responseMessage;

    /**
     * Constructor for AppointmentPaymentService
     * @param appointmentPaymentRepository appointmentPaymentRepository
     * @param appointmentInformationRepository appointmentInformationRepository
     * @param expenseRepository expenseRepository
     * @param expenseService expenseService
     */
    public AppointmentPaymentService(AppointmentPaymentRepository appointmentPaymentRepository,
                                     AppointmentInformationRepository appointmentInformationRepository,
                                     ExpenseRepository expenseRepository, ExpenseService expenseService) {
        this.appointmentPaymentRepository = appointmentPaymentRepository;
        this.appointmentInformationRepository = appointmentInformationRepository;
        this.expenseRepository = expenseRepository;
        this.expenseService = expenseService;
    }

    /**
     * Get Payment Details
     * @param appointmentId appointmentId
     * @return AppointmentPaymentEntity
     */
    @RequiresLogin
    public AppointmentPaymentEntity getPaymentDetails(String appointmentId) {
        return appointmentPaymentRepository.findPaymentByAppointmentId(appointmentId);
    }

    /**
     * Get list of all payments or payments for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @param showNonReconciled showNonReconciled
     * @return List of payments
     */
    @RequiresLogin
    public Payment getAllPayments(LocalDate startDate, LocalDate endDate, boolean showNonReconciled) {
        List<AppointmentPaymentEntity> resultList = null;
        if(startDate != null && endDate != null) {
            if(showNonReconciled) {
                resultList = appointmentPaymentRepository.getAllNonReconciledPaymentsForDateRange(startDate, endDate);
            } else {
                resultList = appointmentPaymentRepository.getAllAppointmentPaymentsForDateRange(startDate, endDate);
            }
        } else if(startDate == null && endDate == null) {
            //resultList = appointmentPaymentRepository.getAllAppointmentPayments();
            startDate = LocalDate.parse(LocalDate.now().getYear() + "-01-01");
            endDate = LocalDate.parse(LocalDate.now().getYear() + "-12-31");
            if(showNonReconciled) {
                resultList = appointmentPaymentRepository.getAllNonReconciledPaymentsForDateRange(startDate, endDate);
            } else {
                resultList = appointmentPaymentRepository.getAllAppointmentPaymentsForDateRange(startDate, endDate);
            }
        }
        //DateRange dateRange = TimestampUtil.getOffsetDateRange(startDate, endDate);
        //List<AppointmentInformationEntity> appointmentList = appointmentInformationRepository.getAllAppointmentsForDateRange(dateRange.getStartTimestamp(), dateRange.getEndTimestamp());
        List<AppointmentInformationEntity> appointmentList = appointmentInformationRepository.getAllAppointments();
        List<AppointmentInformationEntity> newAppointmentList = new ArrayList<>();
        assert resultList != null;
        for(AppointmentPaymentEntity appointmentPaymentEntity : resultList) {
            String appointmentId = appointmentPaymentEntity.getAppointmentId();
            for(AppointmentInformationEntity appointmentInformationEntity : appointmentList) {
                if(appointmentId.endsWith("-R")) {
                    appointmentId = appointmentId.substring(0, appointmentId.length() - 2);
                }
                if(appointmentInformationEntity.getAppointmentId().equals(appointmentId)) {
                    newAppointmentList.add(appointmentInformationEntity);
                    break;
                }
            }
        }
        return Payment.builder()
                .appointmentPayment(resultList)
                .appointmentInformation(newAppointmentList)
                .build();
    }

    /**
     * Update Payment Details
     * @param appointmentPayment appointmentPayment
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus updatePaymentDetails(AppointmentPaymentEntity appointmentPayment) {
        try {
            appointmentPayment.setServiceAmount(appointmentPayment.getServiceAmount().abs());
            if(appointmentPayment.getBciAmount().compareTo(BigDecimal.ZERO) > 0) {
                appointmentPayment.setBciAmount(appointmentPayment.getBciAmount().negate());
            }
            appointmentPaymentRepository.save(appointmentPayment);
            responseCode = 200;
            responseMessage = "Appointment payment updated successfully.";
        } catch (Exception e) {
            responseCode = 400;
            responseMessage = e.getMessage();
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * Update service amount and comment
     * @param appointmentId appointmentId
     * @param serviceAmount serviceAmount
     * @param paymentComment paymentComment
     */
    @RequiresLogin
    public ApiStatus updateServiceAmountAndComment(String appointmentId, BigDecimal serviceAmount, String paymentComment) {
        responseCode = 200;
        responseMessage = "Appointment service amount updated successfully.";
        AppointmentPaymentEntity appointmentPayment = this.getPaymentDetails(appointmentId);
        if(appointmentPayment.getPaymentStatusCode() == PaymentStatus.Processed.getPaymentStatusCode() &&
                appointmentPayment.getPaymentMethodCode() == PaymentMethod.Card.getPaymentMethodCode() &&
                !appointmentPayment.getPaymentComment().startsWith("Refund") &&
                appointmentPayment.getServiceAmount().compareTo(serviceAmount) != 0) {

            String expenseReferenceNumber = "ApptID-" + appointmentId;
            BigDecimal transactionFees = serviceAmount.multiply(BigDecimal.valueOf(0.026)).add(BigDecimal.valueOf(0.15));
            BigDecimal newServiceAmount = serviceAmount.subtract(transactionFees);
            ExpenseEntity expenseEntity = expenseRepository.findByExpenseReferenceNumber(expenseReferenceNumber);
            if(serviceAmount.compareTo(BigDecimal.ZERO) > 0 && expenseEntity != null) {
                expenseRepository.adjustFee(expenseReferenceNumber, transactionFees.negate());
            } else if(serviceAmount.compareTo(BigDecimal.ZERO) > 0 && expenseEntity == null) {
                LocalDate completeDate = appointmentPayment.getPaymentDate();
                int paymentMethodCode = appointmentPayment.getPaymentMethodCode();
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
                appointmentPaymentRepository.updatePaymentStatusAndMethod(appointmentId, PaymentStatus.Processed.getPaymentStatusCode(),
                        paymentMethodCode, transactionFees);
            } else if(serviceAmount.compareTo(BigDecimal.ZERO) == 0) {
                expenseRepository.removeFee(expenseReferenceNumber);
                newServiceAmount = BigDecimal.ZERO;
            }
            appointmentPaymentRepository.updateServiceAmountAndComment(appointmentId, newServiceAmount.abs(), paymentComment);
        } else if(appointmentPayment.getServiceAmount().compareTo(serviceAmount) != 0 ||
                !appointmentPayment.getPaymentComment().equals(paymentComment)) {
            appointmentPaymentRepository.updateServiceAmountAndComment(appointmentId, serviceAmount.abs(), paymentComment);
        } else {
            responseCode = 409;
            responseMessage = "There is no change done to save.";
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * Reconcile Payment
     * @param appointmentId appointmentId
     * @param paymentReconcileDate paymentReconcileDate
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus reconcilePayment(String appointmentId, LocalDate paymentReconcileDate) {
        responseCode = 409;
        AppointmentPaymentEntity appointmentPayment = appointmentPaymentRepository.findPaymentByAppointmentId(appointmentId);
        if(appointmentPayment.getPaymentStatusCode() != PaymentStatus.Processed.getPaymentStatusCode()
            && appointmentPayment.getPaymentStatusCode() != PaymentStatus.Refunded.getPaymentStatusCode()) {
            responseMessage = "Invalid payment status to reconcile. Current status: " + PaymentStatus.getPaymentStatusName(appointmentPayment.getPaymentStatusCode());
        } else if(paymentReconcileDate.isBefore(appointmentPayment.getPaymentDate())) {
            responseMessage = "Reconcile date must be at the same or after payment date.";
        } else {
            appointmentPaymentRepository.reconcilePayment(appointmentId, paymentReconcileDate,
                    !appointmentPayment.getPaymentComment().startsWith("Refund") ||
                                    appointmentPayment.getPaymentReconcileDate() != null);
            if(appointmentPayment.getPaymentMethodCode() == PaymentMethod.Card.getPaymentMethodCode()) {
                String refNumber = "ApptID-" + appointmentId;
                long expenseId =  expenseRepository.findByExpenseReferenceNumber(refNumber).getExpenseId();
                expenseRepository.reconcileExpense(expenseId, paymentReconcileDate, false);
            }
            responseCode = 200;
            responseMessage = "Payment Reconciled.";
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * Refund Payment
     * @param appointmentId appointmentId
     * @param paymentRefundDate paymentRefundDate
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus refundPayment(String appointmentId, LocalDate paymentRefundDate) {
        responseCode = 409;
        AppointmentPaymentEntity appointmentPayment = appointmentPaymentRepository.findPaymentByAppointmentId(appointmentId);
        if(appointmentPayment.getPaymentStatusCode() != PaymentStatus.Processed.getPaymentStatusCode()) {
            responseMessage = "Invalid payment status to refund. Current status: " + PaymentStatus.getPaymentStatusName(appointmentPayment.getPaymentStatusCode());
        } else if(paymentRefundDate.isBefore(appointmentPayment.getPaymentDate())) {
            responseMessage = "Refund date must be at the same or after payment date.";
        } else {
            BigDecimal refundAmount = appointmentPayment.getServiceAmount();
            if(appointmentPayment.getPaymentMethodCode() == PaymentMethod.Card.getPaymentMethodCode()) {
                String refNumber = "ApptID-" + appointmentId;
                BigDecimal expenseAmount = expenseService.refundFee(refNumber, paymentRefundDate);
                refundAmount = refundAmount.add(expenseAmount.abs());
            }

            String refundMessage = "Refund payment transaction ($" + refundAmount.toString() + ").";
            appointmentPayment.setPaymentComment(refundMessage);
            appointmentPayment.setPaymentUpdate(appointmentPayment.getPaymentReconcileDate() == null);
            appointmentPaymentRepository.save(appointmentPayment);
            AppointmentPaymentEntity newAppointmentPayment = AppointmentPaymentEntity.builder()
                    .appointmentId(appointmentId + "-R")
                    .serviceAmount(appointmentPayment.getServiceAmount().negate())
                    .bciAmount(BigDecimal.ZERO)
                    .paymentStatusCode(PaymentStatus.Refunded.getPaymentStatusCode())
                    .paymentMethodCode(appointmentPayment.getPaymentMethodCode())
                    .paymentDate(paymentRefundDate)
                    .paymentComment(refundMessage)
                    .paymentUpdate(appointmentPayment.getPaymentComment().startsWith("Refund") ||
                            appointmentPayment.getPaymentReconcileDate() != null)
                    .build();
            appointmentPaymentRepository.save(newAppointmentPayment);
            responseCode = 200;
            responseMessage = "Refund payment transaction successful for ($" + refundAmount + ").";
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

}
