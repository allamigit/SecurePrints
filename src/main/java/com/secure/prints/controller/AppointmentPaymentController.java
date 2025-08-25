package com.secure.prints.controller;

import com.secure.prints.database.entity.AppointmentPaymentEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.service.AppointmentPaymentService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "payment")
public class AppointmentPaymentController {

    private final AppointmentPaymentService appointmentPaymentService;

    /**
     * Constructor for AppointmentPaymentController
     * @param appointmentPaymentService appointmentPaymentService
     */
    public AppointmentPaymentController(AppointmentPaymentService appointmentPaymentService) {
        this.appointmentPaymentService = appointmentPaymentService;
    }

    /**
     * Get Payment Details
     * @param appointmentId appointmentId
     * @return AppointmentPaymentEntity
     */
    @GetMapping(value = "payment-details", produces = MediaType.APPLICATION_JSON_VALUE)
    public AppointmentPaymentEntity getPaymentDetails(@RequestParam(name = "appointmentId") String appointmentId) {
        return appointmentPaymentService.getPaymentDetails(appointmentId);
    }

    /**
     * Get list of all payments or payments for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @param showNonReconciled showNonReconciled
     * @return List of payments
     */
    @GetMapping(value = "all-payments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AppointmentPaymentEntity> getAllPayments(@RequestParam(name = "startDate", required = false) LocalDate startDate,
                                                         @RequestParam(name = "endDate", required = false) LocalDate endDate,
                                                         @RequestParam(name = "showNonReconciled", required = false) boolean showNonReconciled) {
        return appointmentPaymentService.getAllPayments(startDate, endDate, showNonReconciled);
    }

    /**
     * Update Payment Details
     * @param appointmentPayment appointmentPayment
     * @return ApiStatus
     */
    @PutMapping(value = "update-payment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus updatePaymentDetails(HttpServletResponse response,
                                          @RequestBody AppointmentPaymentEntity appointmentPayment) {
        ApiStatus apiStatus = appointmentPaymentService.updatePaymentDetails(appointmentPayment);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Update service amount and comment
     * @param appointmentId appointmentId
     * @param serviceAmount serviceAmount
     * @param paymentComment paymentComment
     */
    @PatchMapping(value = "update-amount-comment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus updateServiceAmountAndComment(HttpServletResponse response,
                                                   @RequestParam(name = "appointmentId") String appointmentId,
                                                   @RequestParam(name = "serviceAmount") BigDecimal serviceAmount,
                                                   @RequestParam(name = "paymentComment") String paymentComment) {
        ApiStatus apiStatus = appointmentPaymentService.updateServiceAmountAndComment(appointmentId, serviceAmount, paymentComment);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Reconcile Payment
     * @param appointmentId appointmentId
     * @param paymentReconcileDate paymentReconcileDate
     * @return ApiStatus
     */
    @PatchMapping(value = "reconcile-payment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus reconcilePayment(HttpServletResponse response,
                                      @RequestParam(name = "appointmentId") String appointmentId,
                                      @RequestParam(name = "paymentReconcileDate") LocalDate paymentReconcileDate) {
        ApiStatus apiStatus = appointmentPaymentService.reconcilePayment(appointmentId, paymentReconcileDate);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Refund Payment
     * @param appointmentId appointmentId
     * @param paymentRefundDate paymentRefundDate
     * @return ApiStatus
     */
    @PostMapping(value = "refund-payment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus refundPayment(HttpServletResponse response,
                                   @RequestParam(name = "appointmentId") String appointmentId,
                                   @RequestParam(name = "paymentRefundDate") LocalDate paymentRefundDate) {
        ApiStatus apiStatus = appointmentPaymentService.refundPayment(appointmentId, paymentRefundDate);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

}
