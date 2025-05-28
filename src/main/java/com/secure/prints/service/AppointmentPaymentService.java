package com.secure.prints.service;

import com.secure.prints.config.RequiresLogin;
import com.secure.prints.database.AppointmentPaymentRepository;
import com.secure.prints.database.ExpenseRepository;
import com.secure.prints.database.entity.AppointmentPaymentEntity;
import com.secure.prints.database.entity.ExpenseEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.model.PaymentMethod;
import com.secure.prints.model.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AppointmentPaymentService {

    private final AppointmentPaymentRepository appointmentPaymentRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;
    private int responseCode;
    private String responseMessage;

    /**
     * Constructor for AppointmentPaymentService
     * @param appointmentPaymentRepository appointmentPaymentRepository
     * @param expenseRepository expenseRepository
     * @param expenseService expenseService
     */
    public AppointmentPaymentService(AppointmentPaymentRepository appointmentPaymentRepository,
                                     ExpenseRepository expenseRepository,
                                     ExpenseService expenseService) {
        this.appointmentPaymentRepository = appointmentPaymentRepository;
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
    public List<AppointmentPaymentEntity> getAllPayments(LocalDate startDate, LocalDate endDate, boolean showNonReconciled) {
        List<AppointmentPaymentEntity> resultList = null;
        if(startDate != null && endDate != null) {
            if(showNonReconciled) {
                resultList = appointmentPaymentRepository.getAllNonReconciledPaymentsForDateRange(startDate, endDate);
            } else {
                resultList = appointmentPaymentRepository.getAllAppointmentPaymentsForDateRange(startDate, endDate);
            }
        } else if(startDate == null && endDate == null) {
            resultList = appointmentPaymentRepository.getAllAppointmentPayments();
        }
        return resultList;
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
            appointmentPayment.setBciAmount(appointmentPayment.getBciAmount().abs());
            appointmentPaymentRepository.save(appointmentPayment);
            responseCode = 200;
            responseMessage = "Appointment updated successfully.";
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
     * Reconcile Payment
     * @param appointmentId appointmentId
     * @param paymentReconcileDate paymentReconcileDate
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus reconcilePayment(String appointmentId, LocalDate paymentReconcileDate) {
        responseCode = 409;
        AppointmentPaymentEntity appointmentPayment = appointmentPaymentRepository.findPaymentByAppointmentId(appointmentId);
        if(appointmentPayment.getPaymentStatusCode() != PaymentStatus.Processed.getPaymentStatusCode()) {
            responseMessage = "Invalid payment status to reconcile. Current status: " + PaymentStatus.getPaymentStatusName(appointmentPayment.getPaymentStatusCode());
        } else if(paymentReconcileDate.isBefore(appointmentPayment.getPaymentDate())) {
            responseMessage = "Reconcile date must be at the same or after payment date.";
        } else {
            appointmentPaymentRepository.reconcilePayment(appointmentId, paymentReconcileDate);
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
            appointmentPayment.setPaymentUpdate(false);
            appointmentPaymentRepository.save(appointmentPayment);
            AppointmentPaymentEntity newAppointmentPayment = AppointmentPaymentEntity.builder()
                    .appointmentId(appointmentId + "-R")
                    .serviceAmount(appointmentPayment.getServiceAmount().negate())
                    .bciAmount(appointmentPayment.getBciAmount().negate())
                    .paymentStatusCode(PaymentStatus.Refunded.getPaymentStatusCode())
                    .paymentMethodCode(appointmentPayment.getPaymentMethodCode())
                    .paymentDate(paymentRefundDate)
                    .paymentComment("Refund payment transaction.")
                    .paymentUpdate(false)
                    .build();
            appointmentPaymentRepository.save(newAppointmentPayment);
            BigDecimal refundAmount = appointmentPayment.getServiceAmount();
            if(appointmentPayment.getPaymentMethodCode() == PaymentMethod.Card.getPaymentMethodCode()) {
                ExpenseEntity expenseEntity = expenseRepository.findByExpenseReferenceNumber("ApptID-" + appointmentId);
                expenseService.refundExpense(expenseEntity.getExpenseId(), paymentRefundDate);
                refundAmount = refundAmount.add(expenseEntity.getExpenseAmount().abs());
            }
            responseCode = 200;
            responseMessage = "Refund payment transaction successful for ($ " + refundAmount.toString() + ").";
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

}
