package com.secure.prints.service;

import com.secure.prints.config.RequiresLogin;
import com.secure.prints.database.InvoiceRepository;
import com.secure.prints.database.entity.InvoiceEntity;
import com.secure.prints.model.ApiStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private int responseCode;
    private String responseMessage;

    /**
     * Constructor for InvoiceService
     * @param invoiceRepository invoiceRepository
     */
    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Add new invoice details
     * @param invoice invoice
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus addInvoiceDetails(InvoiceEntity invoice) {
        try {
            if(invoice.getInvoiceAmount().compareTo(BigDecimal.ZERO) > 0) {
                invoice.setInvoiceAmount(invoice.getInvoiceAmount().negate());
            }
            if(invoice.getInvoicePaymentDate() != null && invoice.getInvoicePaymentDate().isBefore(invoice.getInvoiceDate())) {
                responseCode = 409;
                responseMessage = "Payment date must be at the same or after invoice date.";
            } else {
                invoice.setInvoiceNumber(invoice.getInvoiceNumber().toUpperCase().trim());
                invoiceRepository.save(invoice);
                responseCode = 201;
                responseMessage = "Invoice Added.";
            }
        } catch (Exception e) {
            responseCode = 400;
            responseMessage = e.getMessage();
            if(responseMessage.contains("unique constraint")) {
                responseMessage = "Duplicate invoice number.";
            }
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * Get Invoice Details
     * @param invoiceId invoiceId
     * @return InvoiceEntity
     */
    @RequiresLogin
    public InvoiceEntity getInvoiceDetails(long invoiceId) {
        return invoiceRepository.findByInvoiceId(invoiceId);
    }

    /**
     * Get list of all invoices or invoices for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @param showNonReconciled showNonReconciled
     * @return List of invoices
     */
    @RequiresLogin
    public List<InvoiceEntity> getAllInvoices(LocalDate startDate, LocalDate endDate, boolean showNonReconciled) {
        List<InvoiceEntity> resultList = null;
        if(startDate != null && endDate != null) {
            if(showNonReconciled) {
                resultList = invoiceRepository.getNonReconciledInvoicesForDateRange(startDate, endDate);
            } else {
                resultList = invoiceRepository.getAllInvoicesForDateRange(startDate, endDate);
            }
        } else if(startDate == null && endDate == null) {
            //resultList = invoiceRepository.getAllInvoices();
            startDate = LocalDate.parse(LocalDate.now().getYear() + "-01-01");
            endDate = LocalDate.parse(LocalDate.now().getYear() + "-12-31");
            if(showNonReconciled) {
                resultList = invoiceRepository.getNonReconciledInvoicesForDateRange(startDate, endDate);
            } else {
                resultList = invoiceRepository.getAllInvoicesForDateRange(startDate, endDate);
            }
        }
        return resultList;
    }

    /**
     * Update Invoice Details
     * @param invoice invoice
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus updateInvoiceDetails(InvoiceEntity invoice) {
        try {
            if(invoice.getInvoiceAmount().compareTo(BigDecimal.ZERO) > 0) {
                invoice.setInvoiceAmount(invoice.getInvoiceAmount().negate());
            }
            if(invoice.getInvoicePaymentDate() != null && invoice.getInvoicePaymentDate().isBefore(invoice.getInvoiceDate())) {
                responseCode = 409;
                responseMessage = "Payment date must be at the same or after invoice date.";
            } else {
                invoice.setInvoiceNumber(invoice.getInvoiceNumber().toUpperCase().trim());
                invoiceRepository.save(invoice);
                responseCode = 200;
                responseMessage = "Invoice Updated.";
            }
        } catch (Exception e) {
            responseCode = 400;
            responseMessage = e.getMessage();
            if(responseMessage.contains("unique constraint")) {
                responseMessage = "Duplicate invoice number.";
            }
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * Reconcile Invoice
     * @param invoiceId invoiceId
     * @param invoiceReconcileDate invoiceReconcileDate
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus reconcileInvoice(long invoiceId, LocalDate invoiceReconcileDate) {
        InvoiceEntity invoice = invoiceRepository.findByInvoiceId(invoiceId);
        if(invoiceReconcileDate.isBefore(invoice.getInvoicePaymentDate())) {
            responseCode = 409;
            responseMessage = "Reconcile date must be at the same or after payment date.";
        } else {
            invoiceRepository.reconcileInvoice(invoiceId, invoiceReconcileDate);
            responseCode = 200;
            responseMessage = "Invoice Reconciled.";
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

}
