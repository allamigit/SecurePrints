package com.secure.prints.controller;

import com.secure.prints.service.AppointmentPaymentService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "payment")
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

}
