package com.secure.prints.service;

import com.secure.prints.config.RequiresLogin;
import com.secure.prints.database.AppointmentPaymentRepository;
import com.secure.prints.database.ExpenseRepository;
import com.secure.prints.database.InvoiceRepository;
import com.secure.prints.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinancialReportService {

    private final AppointmentPaymentRepository appointmentPaymentRepository;
    private final ExpenseRepository expenseRepository;
    private final InvoiceRepository invoiceRepository;

    /**
     * Constructor for FinancialReportService
     * @param appointmentPaymentRepository appointmentPaymentRepository
     * @param expenseRepository expenseRepository
     * @param invoiceRepository invoiceRepository
     */
    public FinancialReportService(AppointmentPaymentRepository appointmentPaymentRepository, ExpenseRepository expenseRepository, InvoiceRepository invoiceRepository) {
        this.appointmentPaymentRepository = appointmentPaymentRepository;
        this.expenseRepository = expenseRepository;
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Generate financial report for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @return FinancialReport
     */
    @RequiresLogin
    public FinancialReport generateFinancialReport(LocalDate startDate, LocalDate endDate) {
        if(startDate == null && endDate == null) {
            startDate = LocalDate.parse(LocalDate.now().getYear() + "-01-01");
            endDate = LocalDate.parse(LocalDate.now().getYear() + "-12-31");
        }
        BigDecimal totalServiceAmount = appointmentPaymentRepository.getTotalServiceAmountAll(startDate, endDate);
        totalServiceAmount = totalServiceAmount == null ? BigDecimal.ZERO : totalServiceAmount;
        BigDecimal totalBciAmount = appointmentPaymentRepository.getTotalBciAmountAll(startDate, endDate);
        totalBciAmount = totalBciAmount == null ? BigDecimal.ZERO : totalBciAmount.negate();
        BigDecimal totalBankFeesAmount = expenseRepository.getTotalBankFeesAmountAll(startDate, endDate);
        totalBankFeesAmount = totalBankFeesAmount == null ? BigDecimal.ZERO : totalBankFeesAmount.negate();
        BigDecimal totalRevenue = totalServiceAmount.add(totalBankFeesAmount.abs());
        BigDecimal grossProfit = totalRevenue.add(totalBciAmount).add(totalBankFeesAmount);
        BigDecimal totalExpenseAmount = expenseRepository.getTotalExpenseAmountAll(startDate, endDate);
        totalExpenseAmount = totalExpenseAmount == null ? BigDecimal.ZERO : totalExpenseAmount.negate();
        BigDecimal netProfit = grossProfit.add(totalExpenseAmount);
        BigDecimal totalInvoicedAmount = invoiceRepository.getTotalInvoiceAmountAll(startDate, endDate);
        totalInvoicedAmount = totalInvoicedAmount == null ? BigDecimal.ZERO : totalInvoicedAmount;
        BigDecimal totalNotInvoicedAmount = totalBciAmount.add(totalInvoicedAmount);

        Revenue revenueAll = Revenue.builder()
                .totalRevenue(totalRevenue)
                .bciFees(totalBciAmount)
                .bankFees(totalBankFeesAmount)
                .grossProfit(grossProfit)
                .totalExpense(totalExpenseAmount)
                .netProfit(netProfit)
                .totalInvoiced(totalInvoicedAmount)
                .totalNotInvoiced(totalNotInvoicedAmount)
                .build();

        totalServiceAmount = appointmentPaymentRepository.getTotalServiceAmountProcessed(startDate, endDate);
        totalServiceAmount = totalServiceAmount == null ? BigDecimal.ZERO : totalServiceAmount;
        totalBciAmount = appointmentPaymentRepository.getTotalBciAmountProcessed(startDate, endDate);
        totalBciAmount = totalBciAmount == null ? BigDecimal.ZERO : totalBciAmount.negate();
        totalBankFeesAmount = expenseRepository.getTotalBankFeesAmountProcessed(startDate, endDate);
        totalBankFeesAmount = totalBankFeesAmount == null ? BigDecimal.ZERO : totalBankFeesAmount.negate();
        totalRevenue = totalServiceAmount.add(totalBankFeesAmount.abs());
        grossProfit = totalRevenue.add(totalBciAmount).add(totalBankFeesAmount);
        totalExpenseAmount = expenseRepository.getTotalExpenseAmountProcessed(startDate, endDate);
        totalExpenseAmount = totalExpenseAmount == null ? BigDecimal.ZERO : totalExpenseAmount.negate();
        netProfit = grossProfit.add(totalExpenseAmount);
        totalInvoicedAmount = invoiceRepository.getTotalInvoiceAmountProcessed(startDate, endDate);
        totalInvoicedAmount = totalInvoicedAmount == null ? BigDecimal.ZERO : totalInvoicedAmount;
        totalNotInvoicedAmount = totalBciAmount.add(totalInvoicedAmount);

        Revenue revenueProcessed = Revenue.builder()
                .totalRevenue(totalRevenue)
                .bciFees(totalBciAmount)
                .bankFees(totalBankFeesAmount)
                .grossProfit(grossProfit)
                .totalExpense(totalExpenseAmount)
                .netProfit(netProfit)
                .totalInvoiced(totalInvoicedAmount)
                .totalNotInvoiced(totalNotInvoicedAmount)
                .build();

        return FinancialReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .revenueAll(revenueAll)
                .revenueProcessed(revenueProcessed)
                .build();
    }

    /**
     * Generate expense report for a specific date range grouped by category and subcategory
     * @param startDate startDate
     * @param endDate endDate
     * @return List of expense report
     */
    @RequiresLogin
    public ExpenseFullReport generateExpenseReport(LocalDate startDate, LocalDate endDate) {
        if(startDate == null && endDate == null) {
            startDate = LocalDate.parse(LocalDate.now().getYear() + "-01-01");
            endDate = LocalDate.parse(LocalDate.now().getYear() + "-12-31");
        }
        List<ExpenseResultset> expenseTotalsAll = expenseRepository.getExpenseTotalsAll(startDate, endDate);
        List<ExpenseReport> expenseReportAllList = new ArrayList<>();
        ExpenseReport expenseReport;
        List<ExpenseSubcategoryTotal> expenseSubcategoryTotalList;
        for(int i = 0; i < expenseTotalsAll.size(); i++) {
            int catCode = expenseTotalsAll.get(i).getCategoryCode();
            int subcatCode = expenseTotalsAll.get(i).getSubcategoryCode();
            expenseReport = ExpenseReport.builder()
                    .expenseCategory(new ExpenseCategory(catCode, ExpenseTypeService.getExpenseTypeName(catCode, subcatCode).getCategoryName()))
                    .build();
            expenseSubcategoryTotalList = new ArrayList<>();
            int j = i;
            do {
                subcatCode = expenseTotalsAll.get(j).getSubcategoryCode();
                expenseSubcategoryTotalList.add(new ExpenseSubcategoryTotal(subcatCode,
                        ExpenseTypeService.getExpenseTypeName(catCode, subcatCode).getSubcategoryName(),
                        expenseTotalsAll.get(j).getSubcategoryTotal()));
                i = j; j++;
            } while(j < expenseTotalsAll.size() && catCode == expenseTotalsAll.get(j).getCategoryCode());
            expenseReport.setExpenseSubcategoriesTotal(expenseSubcategoryTotalList);
            expenseReportAllList.add(expenseReport);
        }

        List<ExpenseResultset> expenseTotalsProcessed = expenseRepository.getExpenseTotalsProcessed(startDate, endDate);
        List<ExpenseReport> expenseReportProcessedList = new ArrayList<>();
        for(int i = 0; i < expenseTotalsProcessed.size(); i++) {
            int catCode = expenseTotalsProcessed.get(i).getCategoryCode();
            int subcatCode = expenseTotalsProcessed.get(i).getSubcategoryCode();
            expenseReport = ExpenseReport.builder()
                    .expenseCategory(new ExpenseCategory(catCode, ExpenseTypeService.getExpenseTypeName(catCode, subcatCode).getCategoryName()))
                    .build();
            expenseSubcategoryTotalList = new ArrayList<>();
            int j = i;
            do {
                subcatCode = expenseTotalsProcessed.get(j).getSubcategoryCode();
                expenseSubcategoryTotalList.add(new ExpenseSubcategoryTotal(subcatCode,
                        ExpenseTypeService.getExpenseTypeName(catCode, subcatCode).getSubcategoryName(),
                        expenseTotalsProcessed.get(j).getSubcategoryTotal()));
                i = j; j++;
            } while(j < expenseTotalsProcessed.size() && catCode == expenseTotalsProcessed.get(j).getCategoryCode());
            expenseReport.setExpenseSubcategoriesTotal(expenseSubcategoryTotalList);
            expenseReportProcessedList.add(expenseReport);
        }

        return ExpenseFullReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .expenseReportAll(expenseReportAllList)
                .expenseReportProcessed(expenseReportProcessedList)
                .build();
    }

}
