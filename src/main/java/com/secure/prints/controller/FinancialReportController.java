package com.secure.prints.controller;

import com.secure.prints.model.FinancialReport;
import com.secure.prints.service.FinancialReportService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping(value = "report")
public class FinancialReportController {

    private final FinancialReportService financialReportService;

    /**
     * Constructor for FinancialReportController
     * @param financialReportService financialReportService
     */
    public FinancialReportController(FinancialReportService financialReportService) {
        this.financialReportService = financialReportService;
    }

    /**
     * Generate financial report for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @return FinancialReport
     */
    @GetMapping(value = "generate-report", produces = MediaType.APPLICATION_JSON_VALUE)
    public FinancialReport generateFinancialReport(@RequestParam(name = "startDate") LocalDate startDate,
                                                   @RequestParam(name = "endDate") LocalDate endDate) {
        return financialReportService.generateFinancialReport(startDate, endDate);
    }

}
