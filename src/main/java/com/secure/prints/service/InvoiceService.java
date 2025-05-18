package com.secure.prints.service;

import com.secure.prints.database.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    /**
     * Constructor for InvoiceService
     * @param invoiceRepository invoiceRepository
     */
    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

}
