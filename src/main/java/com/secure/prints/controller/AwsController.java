package com.secure.prints.controller;

import com.secure.prints.model.ApiStatus;
import com.secure.prints.model.AppointmentResponse;
import com.secure.prints.model.ContactEmail;
import com.secure.prints.service.AwsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
     * Send customer message to Admin team
     * @param contactEmail contactEmail
     */
    @PostMapping(value = "send-contact-email", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus sendContactEmail(HttpServletResponse response, @RequestBody ContactEmail contactEmail) {
        ApiStatus apiStatus =  awsService.sendContactEmail(contactEmail);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

}
