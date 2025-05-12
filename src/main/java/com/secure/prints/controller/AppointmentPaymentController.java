package com.secure.prints.controller;

import com.secure.prints.model.ExpenseType;
import com.secure.prints.service.AppointmentPaymentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "expense")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
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
     * Generate Expense Type list for categories and subcategories
     * @return List of ExpenseType
     */
    @GetMapping(value = "expense-type-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public static List<ExpenseType> generateExpenseTypeList() {
        return AppointmentPaymentService.generateExpenseTypeList();
    }

    /**
     * Search Expense Type list for a keyword in category and subcategory
     * @param keyword keyword
     * @return Filtered list for keyword
     */
    @GetMapping(value = "search-expense-type-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public static List<ExpenseType> searchExpenseTypeList(@RequestParam(name = "keyword") String keyword) {
        return AppointmentPaymentService.searchExpenseTypeList(keyword);
    }

}
