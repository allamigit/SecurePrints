package com.secure.prints.controller;

import com.secure.prints.database.entity.ExpenseEntity;
import com.secure.prints.model.ApiResponse;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.model.ExpenseCode;
import com.secure.prints.model.ExpenseType;
import com.secure.prints.service.ExpenseService;
import com.secure.prints.service.ExpenseTypeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "expense")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Constructor for ExpenseController
     * @param expenseService expenseService
     */
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * Add new expense details
     * @param expense expense
     * @param expenseSubcategoryName expenseSubcategoryName
     * @param expensePaymentStatusName expensePaymentStatusName
     * @return ApiStatus
     */
    @PostMapping(value = "add-expense", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus addExpenseDetails(HttpServletResponse response,
                                       @RequestBody ExpenseEntity expense,
                                       @RequestParam(name = "expenseSubcategoryName") String expenseSubcategoryName,
                                       @RequestParam(name = "expensePaymentStatusName") String expensePaymentStatusName) {
        ApiStatus apiStatus = expenseService.addExpenseDetails(expense, expenseSubcategoryName, expensePaymentStatusName);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Get Expense Details
     * @param expenseId expenseId
     * @return ExpenseEntity
     */
    @GetMapping(value = "expense-details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExpenseEntity getExpenseDetails(@RequestParam(name = "expenseId") long expenseId) {
        return expenseService.getExpenseDetails(expenseId);
    }

    /**
     * Generate Expense Type list for categories and subcategories
     * @return List of ExpenseType
     */
    @GetMapping(value = "expense-type-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public static List<ExpenseType> generateExpenseTypeList() {
        return ExpenseTypeService.generateExpenseTypeList();
    }

    /**
     * Search Expense Type list for a keyword in category and subcategory
     * @param keyword keyword
     * @return Filtered list for keyword
     */
    @GetMapping(value = "search-expense-type-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public static List<ExpenseType> searchExpenseTypeList(@RequestParam(name = "keyword") String keyword) {
        return ExpenseTypeService.searchExpenseTypeList(keyword);
    }

    /**
     * Returns expense category and subcategory codes
     * @param subcategoryName subcategoryName
     * @return Optional of codes value
     */
    @GetMapping(value = "get-expense-code", produces = MediaType.APPLICATION_JSON_VALUE)
    public static Optional<ExpenseCode> getExpenseCode(@RequestParam(name = "subcategoryName") String subcategoryName) {
        return ExpenseTypeService.getExpenseCode(subcategoryName);
    }

}
