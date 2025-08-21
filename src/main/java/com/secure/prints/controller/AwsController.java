package com.secure.prints.controller;

import com.secure.prints.model.AppointmentResponse;
import com.secure.prints.service.AwsService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "aws")
public class AwsController {

    private final AwsService awsService;

    /**
     * Constructor for AwsController
     * @param awsService awsService
     */
    public AwsController(AwsService awsService) {
        this.awsService = awsService;
    }

    /**
     * Send confirmation email to customer
     * @param firstName firstName
     * @param emailTo emailTo
     * @param appointmentResponse appointmentResponse
     */
    @PostMapping(value = "send-confirmation-email", produces = MediaType.APPLICATION_JSON_VALUE)
    public void sendConfirmationEmail(@RequestParam(name = "firstName") String firstName,
                                      @RequestParam(name = "emailTo") String emailTo,
                                      @RequestParam(name = "appointmentResponse") AppointmentResponse appointmentResponse) {
        awsService.sendConfirmationEmail(firstName, emailTo, appointmentResponse);
    }

}
