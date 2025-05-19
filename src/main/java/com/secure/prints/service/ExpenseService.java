package com.secure.prints.service;

import com.secure.prints.config.RequiresLogin;
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
    private int responseCode;
    private String responseMessage;

    /**
     * Constructor for ExpenseService
     * @param expenseRepository expenseRepository
     */
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
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
                expense.setExpenseId(expenseRepository.getNextExpenseId());
                expense.setExpenseUpdate(true);
                expenseRepository.save(expense);
                responseCode = 200;
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
            expenseRepository.reconcileExpense(expenseId, expenseReconcileDate);
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
        ExpenseEntity expense = expenseRepository.findByExpenseId(expenseId);
        if(expenseRefundDate.isBefore(expense.getExpensePaymentDate())) {
            responseCode = 409;
            responseMessage = "Refund date must be at the same or after payment date.";
        } else {
            expense.setExpenseUpdate(false);
            expenseRepository.save(expense);
            ExpenseEntity newExpense = ExpenseEntity.builder()
                    .expenseId(expenseRepository.getNextExpenseId())
                    .expensePayeeName(expense.getExpensePayeeName())
                    .expenseReferenceNumber(expense.getExpenseReferenceNumber() + "-R")
                    .expenseReferenceDate(expense.getExpenseReferenceDate())
                    .expenseCategoryCode(expense.getExpenseCategoryCode())
                    .expenseSubcategoryCode(expense.getExpenseSubcategoryCode())
                    .expenseDescription("Refund expense transaction.")
                    .expenseAmount(expense.getExpenseAmount().abs())
                    .expensePaymentStatusCode(PaymentStatus.Refunded.getPaymentStatusCode())
                    .expensePaymentDate(expenseRefundDate)
                    .expensePaymentMethodCode(expense.getExpensePaymentMethodCode())
                    .expenseDocumentFileName(expense.getExpenseDocumentFileName())
                    .expenseUpdate(false)
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

}
