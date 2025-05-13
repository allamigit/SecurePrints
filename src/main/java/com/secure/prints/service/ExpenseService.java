package com.secure.prints.service;

import com.secure.prints.database.ExpenseRepository;
import com.secure.prints.database.entity.ExpenseEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.model.ExpenseCode;
import com.secure.prints.model.ExpenseType;
import com.secure.prints.model.PaymentStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
     * @param expenseSubcategoryName expenseSubcategoryName
     * @param expensePaymentStatusName expensePaymentStatusName
     * @return ApiStatus
     */
    public ApiStatus addExpenseDetails(ExpenseEntity expense, String expenseSubcategoryName, String expensePaymentStatusName) {
        try {
            expense.setExpenseId(expenseRepository.getNextExpenseId());
            expense.setExpensePaymentStatusCode(PaymentStatus.getPaymentStatusCode(expensePaymentStatusName));
            if(expense.getExpenseCategoryCode() == null) {
                Optional<ExpenseCode> expenseCode = ExpenseTypeService.getExpenseCode(expenseSubcategoryName);
                expense.setExpenseCategoryCode(expenseCode.isPresent() ? expenseCode.get().getCategoryCode() : 0);
                expense.setExpenseSubcategoryCode(expenseCode.isPresent() ? expenseCode.get().getSubcategoryCode() : 0);
            }
            expenseRepository.save(expense);
            responseCode = 200;
            responseMessage = "Expense Added: " + expenseSubcategoryName;
        } catch (Exception e) {
            responseCode = 400;
            responseMessage = e.getCause().getMessage();
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
        Optional<ExpenseEntity> expenseEntity = expenseRepository.findById(expenseId);
        return expenseEntity.orElse(null);
    }

}
