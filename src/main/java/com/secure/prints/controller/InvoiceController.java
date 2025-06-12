package com.secure.prints.controller;

import com.secure.prints.service.InvoiceService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "invoice")
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * Constructor for
     * @param invoiceService InvoiceController
     */
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

}
