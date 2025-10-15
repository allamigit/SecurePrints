package com.secure.prints.controller;

import com.secure.prints.database.entity.InvoiceEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.service.InvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "invoice")
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * Constructor for InvoiceController
     * @param invoiceService invoiceService
     */
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Add new invoice details
     * @param invoice invoice
     * @return ApiStatus
     */
    @PostMapping(value = "add-invoice", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus addInvoiceDetails(HttpServletResponse response,
                                       @RequestBody InvoiceEntity invoice) {
        ApiStatus apiStatus = invoiceService.addInvoiceDetails(invoice);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Get Invoice Details
     * @param invoiceId invoiceId
     * @return InvoiceEntity
     */
    @GetMapping(value = "invoice-details", produces = MediaType.APPLICATION_JSON_VALUE)
    public InvoiceEntity getInvoiceDetails(@RequestParam(name = "invoiceId") long invoiceId) {
        return invoiceService.getInvoiceDetails(invoiceId);
    }

    /**
     * Get list of all invoices or invoices for a specific date range
     * @param startDate startDate
     * @param endDate endDate
     * @param showNonReconciled showNonReconciled
     * @return List of invoices
     */
    @GetMapping(value = "all-invoices", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<InvoiceEntity> getAllInvoices(@RequestParam(name = "startDate", required = false) LocalDate startDate,
                                              @RequestParam(name = "endDate", required = false) LocalDate endDate,
                                              @RequestParam(name = "showNonReconciled", required = false) boolean showNonReconciled) {
        return invoiceService.getAllInvoices(startDate, endDate, showNonReconciled);
    }

    /**
     * Update Invoice Details
     * @param invoice invoice
     * @return ApiStatus
     */
    @PutMapping(value = "update-invoice", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus updateInvoiceDetails(HttpServletResponse response,
                                          @RequestBody InvoiceEntity invoice) {
        ApiStatus apiStatus = invoiceService.updateInvoiceDetails(invoice);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Upload invoice file to S3 bucket
     * @param file file
     * @return File path in S3 bucket
     * @throws IOException IOException
     */
    @PostMapping(value = "upload-invoice", produces = MediaType.APPLICATION_JSON_VALUE)
    public String uploadInvoiceToS3Bucket(@RequestParam(name = "file") MultipartFile file) throws IOException {
        return invoiceService.uploadInvoiceToS3Bucket(file);
    }

    /**
     * Reconcile Invoice
     * @param invoiceId invoiceId
     * @param invoiceReconcileDate invoiceReconcileDate
     * @return ApiStatus
     */
    @PatchMapping(value = "reconcile-invoice", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus reconcileInvoice(HttpServletResponse response,
                                      @RequestParam(name = "invoiceId") long invoiceId,
                                      @RequestParam(name = "invoiceReconcileDate") LocalDate invoiceReconcileDate) {
        ApiStatus apiStatus = invoiceService.reconcileInvoice(invoiceId, invoiceReconcileDate);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Get invoice due date which is invoice date + 30 days
     * @param invoiceDate invoiceDate
     * @return Invoice Due Date
     */
    @GetMapping(value = "due-date", produces = MediaType.APPLICATION_JSON_VALUE)
    public LocalDate getInvoiceDueDate(@RequestParam(name = "invoiceDate") LocalDate invoiceDate) {
        return invoiceDate.plusDays(30);
    }

}
