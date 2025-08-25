package com.secure.prints.service;

import com.secure.prints.database.AppointmentPaymentRepository;
import com.secure.prints.database.ExpenseRepository;
import com.secure.prints.database.InvoiceRepository;
import com.secure.prints.model.FinancialReport;
import com.secure.prints.model.Revenue;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class FinancialReportService {

    private final AppointmentPaymentRepository appointmentPaymentRepository;
    private final ExpenseRepository expenseRepository;
    private final InvoiceRepository invoiceRepository;

    /*BigDecimal totalServiceAmount = BigDecimal.ZERO;
    BigDecimal totalBciAmount = BigDecimal.ZERO;
    BigDecimal totalBankFeesAmount = BigDecimal.ZERO;
    BigDecimal totalRevenue = BigDecimal.ZERO;
    BigDecimal grossProfit = BigDecimal.ZERO;
    BigDecimal totalExpenseAmount = BigDecimal.ZERO;
    BigDecimal netProfit = BigDecimal.ZERO;
    BigDecimal totalInvoicedAmount = BigDecimal.ZERO;
    BigDecimal totalNotInvoicedAmount = BigDecimal.ZERO;*/

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
        BigDecimal totalServiceAmount = appointmentPaymentRepository.getTotalServiceAmount(startDate, endDate);
        totalServiceAmount = totalServiceAmount == null ? BigDecimal.ZERO : totalServiceAmount;
        BigDecimal totalBciAmount = appointmentPaymentRepository.getTotalBciAmount(startDate, endDate);
        totalBciAmount = totalBciAmount == null ? BigDecimal.ZERO : totalBciAmount;
        BigDecimal totalBankFeesAmount = expenseRepository.getTotalBankFeesAmount(startDate, endDate);
        totalBankFeesAmount = totalBankFeesAmount == null ? BigDecimal.ZERO : totalBankFeesAmount;
        BigDecimal totalRevenue = totalServiceAmount.add(totalBankFeesAmount.abs());
        BigDecimal grossProfit = totalRevenue.add(totalBciAmount).add(totalBankFeesAmount);
        BigDecimal totalExpenseAmount = expenseRepository.getTotalExpenseAmount(startDate, endDate);
        totalExpenseAmount = totalExpenseAmount == null ? BigDecimal.ZERO : totalExpenseAmount;
        BigDecimal netProfit = grossProfit.add(totalExpenseAmount);
        BigDecimal totalInvoicedAmount = invoiceRepository.getTotalInvoiceAmount(startDate, endDate);
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

        Revenue revenueProcessed = Revenue.builder().build();

        return FinancialReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .revenueAll(revenueAll)
                .revenueProcessed(revenueProcessed)
                .build();
    }

}
