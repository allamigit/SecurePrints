package com.secure.prints.controller;

import com.secure.prints.database.entity.ExpenseEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.model.ExpenseTypeCode;
import com.secure.prints.model.ExpenseType;
import com.secure.prints.model.ExpenseTypeName;
import com.secure.prints.service.ExpenseService;
import com.secure.prints.service.ExpenseTypeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "expense")
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
     * @return ApiStatus
     */
    @PostMapping(value = "add-expense", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus addExpenseDetails(HttpServletResponse response,
                                       @RequestBody ExpenseEntity expense) {
        ApiStatus apiStatus = expenseService.addExpenseDetails(expense);
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
     * Get list of all expenses or expenses for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @param showNonReconciled showNonReconciled
     * @return List of expenses
     */
    @GetMapping(value = "all-expenses", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ExpenseEntity> getAllExpenses(@RequestParam(name = "startDate", required = false) LocalDate startDate,
                                              @RequestParam(name = "endDate", required = false) LocalDate endDate,
                                              @RequestParam(name = "showNonReconciled", required = false) boolean showNonReconciled) {
        return expenseService.getAllExpenses(startDate, endDate, showNonReconciled);
    }

    /**
     * Update Expense Details
     * @param expense expense
     * @return ApiStatus
     */
    @PutMapping(value = "update-expense", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus updateExpenseDetails(HttpServletResponse response,
                                          @RequestBody ExpenseEntity expense) {
        ApiStatus apiStatus = expenseService.updateExpenseDetails(expense);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Reconcile Expense
     * @param expenseId expenseId
     * @param expenseReconcileDate expenseReconcileDate
     * @return ApiStatus
     */
    @PatchMapping(value = "reconcile-expense", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus reconcileExpense(HttpServletResponse response,
                                      @RequestParam(name = "expenseId") long expenseId,
                                      @RequestParam(name = "expenseReconcileDate") LocalDate expenseReconcileDate) {
        ApiStatus apiStatus = expenseService.reconcileExpense(expenseId, expenseReconcileDate);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Refund Expense
     * @param expenseId expenseId
     * @param expenseRefundDate expenseRefundDate
     * @return ApiStatus
     */
    @PostMapping(value = "refund-expense", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus refundExpense(HttpServletResponse response,
                                   @RequestParam(name = "expenseId") long expenseId,
                                   @RequestParam(name = "expenseRefundDate") LocalDate expenseRefundDate) {
        ApiStatus apiStatus = expenseService.refundExpense(expenseId, expenseRefundDate);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Generate Expense Type list for categories and subcategories
     * @return List of ExpenseType
     */
    @GetMapping(value = "expense-type-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ExpenseType> generateExpenseTypeList() {
        return ExpenseTypeService.expenseTypeList;
    }

    /**
     * Search Expense Type list for a keyword in category and subcategory
     * @param keyword keyword
     * @return Filtered list for keyword
     */
    @GetMapping(value = "search-expense-type-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ExpenseType> searchExpenseTypeList(@RequestParam(name = "keyword") String keyword) {
        return ExpenseTypeService.searchExpenseTypeList(keyword);
    }

    /**
     * Returns expense category and subcategory codes
     * @param subcategoryName subcategoryName
     * @return Optional of codes value
     */
    @GetMapping(value = "get-expense-type-code", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExpenseTypeCode getExpenseTypeCode(@RequestParam(name = "subcategoryName") String subcategoryName) {
        return ExpenseTypeService.getExpenseTypeCode(subcategoryName);
    }

    /**
     * Returns expense category and subcategory names
     * @param categoryCode categoryCode
     * @param subcategoryCode subcategoryCode
     * @return ExpenseTypeName of codes value
     */
    @GetMapping(value = "get-expense-type-name", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExpenseTypeName getExpenseTypeName(@RequestParam(name = "categoryCode") int categoryCode,
                                              @RequestParam(name = "subcategoryCode") int subcategoryCode) {
        return ExpenseTypeService.getExpenseTypeName(categoryCode, subcategoryCode);
    }

}
