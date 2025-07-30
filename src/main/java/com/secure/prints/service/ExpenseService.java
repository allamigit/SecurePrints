package com.secure.prints.service;

import com.secure.prints.config.RequiresLogin;
import com.secure.prints.database.AppointmentPaymentRepository;
import com.secure.prints.database.ExpenseRepository;
import com.secure.prints.database.entity.ExpenseEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.model.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AppointmentPaymentRepository appointmentPaymentRepository;
    private int responseCode;
    private String responseMessage;

    /**
     * Constructor for ExpenseService
     * @param expenseRepository expenseRepository
     * @param appointmentPaymentRepository appointmentPaymentRepository
     */
    public ExpenseService(ExpenseRepository expenseRepository, AppointmentPaymentRepository appointmentPaymentRepository) {
        this.expenseRepository = expenseRepository;
        this.appointmentPaymentRepository = appointmentPaymentRepository;
    }

    /**
     * Add new expense details
     * @param expense expense
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus addExpenseDetails(ExpenseEntity expense) {
        try {
            if(expense.getExpenseAmount().compareTo(BigDecimal.ZERO) > 0) {
                expense.setExpenseAmount(expense.getExpenseAmount().negate());
            }
            if(expense.getExpensePaymentDate().isBefore(expense.getExpenseReferenceDate())) {
                responseCode = 409;
                responseMessage = "Payment date must be at the same or after reference date.";
            } else {
                expense.setExpenseUpdate(true);
                expenseRepository.save(expense);
                responseCode = 201;
                responseMessage = "Expense Added.";
            }
        } catch (Exception e) {
            responseCode = 400;
            responseMessage = e.getMessage();
            if(responseMessage.contains("unique constraint")) {
                responseMessage = "Duplicate expense reference number.";
            }
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * Get Expense Details
     * @param expenseId expenseId
     * @return ExpenseEntity
     */
    @RequiresLogin
    public ExpenseEntity getExpenseDetails(long expenseId) {
        return expenseRepository.findByExpenseId(expenseId);
    }

    /**
     * Get list of all expenses or expenses for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @param showNonReconciled showNonReconciled
     * @return List of expenses
     */
    @RequiresLogin
    public List<ExpenseEntity> getAllExpenses(LocalDate startDate, LocalDate endDate, boolean showNonReconciled) {
        List<ExpenseEntity> resultList = null;
        if(startDate != null && endDate != null) {
            if(showNonReconciled) {
                resultList = expenseRepository.getNonReconciledExpensesForDateRange(startDate, endDate);
            } else {
                resultList = expenseRepository.getAllExpensesForDateRange(startDate, endDate);
            }
        } else if(startDate == null && endDate == null) {
            resultList = expenseRepository.getAllExpenses();
        }
        return resultList;
    }

    /**
     * Update Expense Details
     * @param expense expense
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus updateExpenseDetails(ExpenseEntity expense) {
        try {
            if(expense.getExpenseAmount().compareTo(BigDecimal.ZERO) > 0) {
                expense.setExpenseAmount(expense.getExpenseAmount().negate());
            }
            if(expense.getExpensePaymentDate().isBefore(expense.getExpenseReferenceDate())) {
                responseCode = 409;
                responseMessage = "Payment date must be at the same or after reference date.";
            } else {
                BigDecimal oldExpenseAmount = this.getExpenseDetails(expense.getExpenseId()).getExpenseAmount().abs();
                BigDecimal newExpenseAmount = expense.getExpenseAmount().abs();
                String expenseReferenceNumber = expense.getExpenseReferenceNumber();
                String appointmentId = expenseReferenceNumber.substring(expenseReferenceNumber.indexOf("-") + 1);
                if(expenseReferenceNumber.startsWith("ApptID-") && !newExpenseAmount.equals(oldExpenseAmount)) {
                    BigDecimal differenceAmount = oldExpenseAmount.subtract(newExpenseAmount);
                    appointmentPaymentRepository.adjustServiceAmount(appointmentId, differenceAmount);
                }
                expenseRepository.save(expense);
                responseCode = 200;
                responseMessage = "Expense Updated.";
            }
        } catch (Exception e) {
            responseCode = 400;
            responseMessage = e.getMessage();
            if(responseMessage.contains("unique constraint")) {
                responseMessage = "Duplicate expense reference number.";
            }
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * Reconcile Expense
     * @param expenseId expenseId
     * @param expenseReconcileDate expenseReconcileDate
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus reconcileExpense(long expenseId, LocalDate expenseReconcileDate) {
        ExpenseEntity expense = expenseRepository.findByExpenseId(expenseId);
        if(expenseReconcileDate.isBefore(expense.getExpensePaymentDate())) {
            responseCode = 409;
            responseMessage = "Reconcile date must be at the same or after payment date.";
        } else {
            expenseRepository.reconcileExpense(expenseId, expenseReconcileDate, !expense.getExpenseDescription().startsWith("Refund"));
            responseCode = 200;
            responseMessage = "Expense Reconciled.";
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * Refund Expense
     * @param expenseId expenseId
     * @param expenseRefundDate expenseRefundDate
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus refundExpense(long expenseId, LocalDate expenseRefundDate) {
        responseCode = 409;
        ExpenseEntity expense = expenseRepository.findByExpenseId(expenseId);
        if(expense.getExpensePaymentStatusCode() != PaymentStatus.Processed.getPaymentStatusCode()) {
            responseMessage = "Invalid payment status to refund. Current status: " + PaymentStatus.getPaymentStatusName(expense.getExpensePaymentStatusCode());
        } else if(expenseRefundDate.isBefore(expense.getExpensePaymentDate())) {
            responseMessage = "Refund date must be at the same or after payment date.";
        } else {
            String refundMessage = "Refund expense transaction.";
            expense.setExpenseUpdate(expense.getExpenseReconcileDate() == null);
            expense.setExpenseDescription(refundMessage);
            expenseRepository.save(expense);
            ExpenseEntity newExpense = ExpenseEntity.builder()
                    .expenseVendorName(expense.getExpenseVendorName())
                    .expenseReferenceNumber(expense.getExpenseReferenceNumber() + "-R")
                    .expenseReferenceDate(expense.getExpenseReferenceDate())
                    .expenseCategoryCode(expense.getExpenseCategoryCode())
                    .expenseSubcategoryCode(expense.getExpenseSubcategoryCode())
                    .expenseDescription(refundMessage)
                    .expenseAmount(expense.getExpenseAmount().abs())
                    .expensePaymentStatusCode(PaymentStatus.Refunded.getPaymentStatusCode())
                    .expensePaymentDate(expenseRefundDate)
                    .expensePaymentMethodCode(expense.getExpensePaymentMethodCode())
                    .expenseDocumentFileName(expense.getExpenseDocumentFileName())
                    .expenseUpdate(expense.getExpenseReconcileDate() != null || expense.getExpenseDescription().startsWith("Refund"))
                    .build();
            expenseRepository.save(newExpense);
            responseCode = 200;
            responseMessage = "Refund expense transaction successful.";
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * Refund CC Reader Fee
     * @param expenseReferenceNumber expenseReferenceNumber
     * @param paymentRefundDate paymentRefundDate
     * @return expenseAmount
     */
    @RequiresLogin
    public BigDecimal refundFee(String expenseReferenceNumber, LocalDate paymentRefundDate) {
        ExpenseEntity expense = expenseRepository.findByExpenseReferenceNumber(expenseReferenceNumber);
        String expenseDescription = "Refund CC Reader fees to customer.";
        expense.setExpenseDescription(expenseDescription);
        expense.setExpenseUpdate(expense.getExpenseReconcileDate() == null);
        expenseRepository.save(expense);
        ExpenseEntity newExpense = ExpenseEntity.builder()
                .expenseVendorName(expense.getExpenseVendorName())
                .expenseReferenceNumber(expense.getExpenseReferenceNumber() + "-R")
                .expenseReferenceDate(expense.getExpenseReferenceDate())
                .expenseCategoryCode(expense.getExpenseCategoryCode())
                .expenseSubcategoryCode(expense.getExpenseSubcategoryCode())
                .expenseDescription(expenseDescription)
                .expenseAmount(expense.getExpenseAmount())
                .expensePaymentStatusCode(PaymentStatus.Refunded.getPaymentStatusCode())
                .expensePaymentDate(paymentRefundDate)
                .expensePaymentMethodCode(expense.getExpensePaymentMethodCode())
                .expenseDocumentFileName(expense.getExpenseDocumentFileName())
                .expenseUpdate(expense.getExpenseReconcileDate() == null)
                .build();
        expenseRepository.save(newExpense);
        return expense.getExpenseAmount();
    }

}
