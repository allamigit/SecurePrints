package com.secure.prints.service;

import com.secure.prints.config.RequiresLogin;
import com.secure.prints.database.InvoiceRepository;
import com.secure.prints.database.entity.InvoiceEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.model.PaymentStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final S3Service s3Service;
    private int responseCode;
    private String responseMessage;

    /**
     * Constructor for InvoiceService
     * @param invoiceRepository invoiceRepository
     * @param s3Service s3Service
     */
    public InvoiceService(InvoiceRepository invoiceRepository, S3Service s3Service) {
        this.invoiceRepository = invoiceRepository;
        this.s3Service = s3Service;
    }

    /**
     * Add new invoice details
     * @param invoice invoice
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus addInvoiceDetails(InvoiceEntity invoice) {
        responseCode = 409;
        InvoiceEntity invoiceEntity = invoiceRepository.findByInvoiceNumber(invoice.getInvoiceNumber());
        try {
            if(invoiceEntity != null) {
                throw new DataIntegrityViolationException("Duplicate invoice number.");
            }

            if(invoice.getInvoiceAmount().compareTo(BigDecimal.ZERO) < 0) {
                invoice.setInvoiceAmount(invoice.getInvoiceAmount().abs());
            }

            if(invoice.getInvoicePaymentStatusCode() == PaymentStatus.Cancelled.getPaymentStatusCode()) {
                responseMessage = "Invalid payment status to cancel new invoice.";
            } else if(invoice.getInvoicePaymentDate() != null && invoice.getInvoicePaymentDate().isBefore(invoice.getInvoiceDate())) {
                responseMessage = "Payment date must be on the same or after invoice date.";
            } else {
                invoice.setInvoiceNumber(invoice.getInvoiceNumber().toUpperCase().trim());
                invoiceRepository.save(invoice);
                responseCode = 201;
                responseMessage = "Invoice Added.";
            }
        } catch (Exception e) {
            responseMessage = e.getMessage();
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
        responseCode = 409;
        InvoiceEntity invoiceEntity = invoiceRepository.findByInvoiceId(invoice.getInvoiceId());
        if(!Objects.equals(invoiceEntity.getInvoiceNumber(), invoice.getInvoiceNumber())) {
            invoiceEntity = invoiceRepository.findByInvoiceNumber(invoice.getInvoiceNumber());
        }
        try {
            if(invoiceEntity != null && !Objects.equals(invoiceEntity.getInvoiceId(), invoice.getInvoiceId())) {
                throw new DataIntegrityViolationException("Duplicate invoice number.");
            }

            if(invoice.getInvoiceAmount().compareTo(BigDecimal.ZERO) < 0) {
                invoice.setInvoiceAmount(invoice.getInvoiceAmount().abs());
            }

            if(invoice.getInvoiceReconcileDate() != null && invoice.getInvoicePaymentStatusCode() != PaymentStatus.Processed.getPaymentStatusCode()) {
                responseMessage = "Invalid payment status to reconcile. Current status: " + PaymentStatus.getPaymentStatusName(invoice.getInvoicePaymentStatusCode());
            } else if(invoice.getInvoicePaymentDate() != null && invoice.getInvoicePaymentDate().isBefore(invoice.getInvoiceDate())) {
                responseMessage = "Payment date must be on the same or after invoice date.";
            } else if(invoice.getInvoicePaymentDate() != null && invoice.getInvoiceReconcileDate() != null && invoice.getInvoiceReconcileDate().isBefore(invoice.getInvoicePaymentDate())) {
                responseMessage = "Reconcile date must be on the same or after payment date.";
            } else {
                invoice.setInvoiceNumber(invoice.getInvoiceNumber().toUpperCase().trim());
                invoiceRepository.save(invoice);
                responseCode = 200;
                responseMessage = "Invoice Updated.";
            }
        } catch (Exception e) {
            responseMessage = e.getMessage();
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * Upload invoice file to S3 bucket
     * @param file file
     * @return File path in S3 bucket
     * @throws IOException IOException
     */
    //@RequiresLogin
    public String uploadInvoiceToS3Bucket(MultipartFile file) throws IOException {
        return s3Service.uploadFile("invoice", file);
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
        if(invoice.getInvoicePaymentDate() != null && invoiceReconcileDate.isBefore(invoice.getInvoicePaymentDate())) {
            responseCode = 409;
            responseMessage = "Reconcile date must be on the same or after payment date.";
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
