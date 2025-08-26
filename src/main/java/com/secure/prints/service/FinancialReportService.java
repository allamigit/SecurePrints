package com.secure.prints.service;

import com.secure.prints.database.AppointmentPaymentRepository;
import com.secure.prints.database.ExpenseRepository;
import com.secure.prints.database.InvoiceRepository;
import com.secure.prints.model.ExpenseReport;
import com.secure.prints.model.FinancialReport;
import com.secure.prints.model.Revenue;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    //@RequiresLogin
    public FinancialReport generateFinancialReport(LocalDate startDate, LocalDate endDate) {
        BigDecimal totalServiceAmount = appointmentPaymentRepository.getTotalServiceAmountAll(startDate, endDate);
        totalServiceAmount = totalServiceAmount == null ? BigDecimal.ZERO : totalServiceAmount;
        BigDecimal totalBciAmount = appointmentPaymentRepository.getTotalBciAmountAll(startDate, endDate);
        totalBciAmount = totalBciAmount == null ? BigDecimal.ZERO : totalBciAmount;
        BigDecimal totalBankFeesAmount = expenseRepository.getTotalBankFeesAmountAll(startDate, endDate);
        totalBankFeesAmount = totalBankFeesAmount == null ? BigDecimal.ZERO : totalBankFeesAmount;
        BigDecimal totalRevenue = totalServiceAmount.add(totalBankFeesAmount.abs());
        BigDecimal grossProfit = totalRevenue.add(totalBciAmount).add(totalBankFeesAmount);
        BigDecimal totalExpenseAmount = expenseRepository.getTotalExpenseAmountAll(startDate, endDate);
        totalExpenseAmount = totalExpenseAmount == null ? BigDecimal.ZERO : totalExpenseAmount;
        BigDecimal netProfit = grossProfit.add(totalExpenseAmount);
        BigDecimal totalInvoicedAmount = invoiceRepository.getTotalInvoiceAmountAll(startDate, endDate);
        totalInvoicedAmount = totalInvoicedAmount == null ? BigDecimal.ZERO : totalInvoicedAmount;
        BigDecimal totalNotInvoicedAmount = totalBciAmount.add(totalInvoicedAmount.abs());

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
        totalBciAmount = totalBciAmount == null ? BigDecimal.ZERO : totalBciAmount;
        totalBankFeesAmount = expenseRepository.getTotalBankFeesAmountProcessed(startDate, endDate);
        totalBankFeesAmount = totalBankFeesAmount == null ? BigDecimal.ZERO : totalBankFeesAmount;
        totalRevenue = totalServiceAmount.add(totalBankFeesAmount.abs());
        grossProfit = totalRevenue.add(totalBciAmount).add(totalBankFeesAmount);
        totalExpenseAmount = expenseRepository.getTotalExpenseAmountProcessed(startDate, endDate);
        totalExpenseAmount = totalExpenseAmount == null ? BigDecimal.ZERO : totalExpenseAmount;
        netProfit = grossProfit.add(totalExpenseAmount);
        totalInvoicedAmount = invoiceRepository.getTotalInvoiceAmountProcessed(startDate, endDate);
        totalInvoicedAmount = totalInvoicedAmount == null ? BigDecimal.ZERO : totalInvoicedAmount;
        totalNotInvoicedAmount = totalBciAmount.add(totalInvoicedAmount.abs());

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
    //@RequiresLogin
    public List<ExpenseReport> generateExpenseReport(LocalDate startDate, LocalDate endDate) {
        ExpenseReport expenseReport = new ExpenseReport();
        return null;
    }

}
