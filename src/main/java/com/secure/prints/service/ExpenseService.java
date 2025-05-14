package com.secure.prints.service;

import com.secure.prints.database.ExpenseRepository;
import com.secure.prints.database.entity.ExpenseEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.model.PaymentStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
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
    public ApiStatus addExpenseDetails(ExpenseEntity expense) {
        try {
            expense.setExpenseId(expenseRepository.getNextExpenseId());
            if(expense.getExpenseAmount().byteValueExact() > 0) {
                expense.setExpenseAmount(expense.getExpenseAmount().multiply(BigDecimal.valueOf(-1)));
            }
            if(expense.getExpensePaymentDate().isBefore(expense.getExpenseReferenceDate())) {
                responseCode = 409;
                responseMessage = "Payment date must be at the same or after reference date.";
            } else {
                expenseRepository.save(expense);
                responseCode = 200;
                responseMessage = "Expense Added.";
            }
        } catch (Exception e) {
            responseCode = 400;
            responseMessage = e.getCause().getMessage();
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
    public ExpenseEntity getExpenseDetails(long expenseId) {
        return expenseRepository.findExpenseById(expenseId);
    }

    /**
     * Get list of all expenses or expenses for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @param showNonReconciled showNonReconciled
     * @return List of expenses
     */
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
    public ApiStatus updateExpenseDetails(ExpenseEntity expense) {
        try {
            if((expense.getExpensePaymentStatusCode() != PaymentStatus.Refunded.getPaymentStatusCode()
                    && expense.getExpenseAmount().byteValueExact() > 0) ||
                    (expense.getExpensePaymentStatusCode() == PaymentStatus.Refunded.getPaymentStatusCode()
                            && expense.getExpenseAmount().byteValueExact() < 0)) {
                expense.setExpenseAmount(expense.getExpenseAmount().multiply(BigDecimal.valueOf(-1)));
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
            responseMessage = e.getCause().getMessage();
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
    public ApiStatus reconcileExpense(long expenseId, LocalDate expenseReconcileDate) {
        ExpenseEntity expense = expenseRepository.findExpenseById(expenseId);
        if(expense.getExpenseReconcileDate().isBefore(expense.getExpensePaymentDate())) {
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
    public ApiStatus refundExpense(long expenseId, LocalDate expenseRefundDate) {
        ExpenseEntity expense = expenseRepository.findExpenseById(expenseId);
        expense.setExpenseId(expenseRepository.getNextExpenseId());
        expense.setExpenseReferenceNumber(expense.getExpenseReferenceNumber() + "-R");
        expense.setExpenseAmount(expense.getExpenseAmount().multiply(BigDecimal.valueOf(-1)));
        expense.setExpensePaymentDate(expenseRefundDate);
        expense.setExpenseDescription("Refund transaction.");
        return ApiStatus.builder()
                .responseCode(200)
                .responseMessage("Refund Successful.")
                .build();
    }

}
