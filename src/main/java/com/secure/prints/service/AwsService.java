package com.secure.prints.service;

import com.secure.prints.model.ApiStatus;
import com.secure.prints.model.AppointmentResponse;
import com.secure.prints.model.ContactEmail;
import com.secure.prints.util.NameFormatUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AwsService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String emailFrom;

    private int responseCode;
    private String responseMessage;

    /**
     * Constructor for AwsService
     * @param mailSender mailSender
     */
    public AwsService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send appointment confirmation email to customer
     * @param customerName customerName
     * @param emailTo emailTo
     * @param appointmentResponse appointmentResponse
     */
    public void sendConfirmationEmail(String customerName, String emailTo, AppointmentResponse appointmentResponse) {
        SimpleMailMessage message = new SimpleMailMessage();
        String reason = "";
        if(!appointmentResponse.getBciReasonCode().isBlank()) {
            reason = "\nBCI Reason Code: " + appointmentResponse.getBciReasonCode();
            reason = reason + "\nBCI Reason Description: " + appointmentResponse.getBciReasonDescription();
        }
        if(!appointmentResponse.getFbiReasonCode().isBlank()) {
            reason = reason + "\nFBI Reason Code: " + appointmentResponse.getFbiReasonCode();
            reason = reason + "\nFBI Reason Description: " + appointmentResponse.getFbiReasonDescription();
        }

        message.setFrom(emailFrom);
        message.setTo(emailTo);
        message.setBcc(emailFrom);
        message.setSubject("SecurePrints: Appointment ID (" + appointmentResponse.getAppointmentId() + ") " + appointmentResponse.getAppointmentStatus());
        message.setText(
                "\nHi " + customerName + "," +
                "\n\nAppointment ID: " + appointmentResponse.getAppointmentId() +
                "\nService Name: " + appointmentResponse.getServiceName() +
                reason +
                "\nAppointment Date and Time: " + appointmentResponse.getAppointmentTimestamp() +
                "\nAppointment Status: " + appointmentResponse.getAppointmentStatus() +
                "\n\nKind Regards,\nSecurePrints Team");
        mailSender.send(message);
    }

    /**
     * Send customer message to Admin team
     * @param contactEmail contactEmail
     */
    public ApiStatus sendContactEmail(ContactEmail contactEmail) {
        contactEmail.setName(NameFormatUtil.formatName(contactEmail.getName()));
        contactEmail.setEmailTo(contactEmail.getEmailTo().toLowerCase());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(emailFrom);
        message.setSubject("SecurePrints: Contact Message from (" + contactEmail.getName() + ")");
        message.setText(
                "\nSender Name: " + contactEmail.getName() +
                "\nSender Email: " + contactEmail.getEmailTo() +
                "\nMessage Text:\n" + contactEmail.getMessageText());
        try {
            mailSender.send(message);
            responseCode = 200;
            responseMessage = "Contact email sent to SecurePrints team.";
        } catch (Exception e) {
            responseCode = 409;
            responseMessage = "Failed sending contact email to SecurePrints team.";
        }
        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

}
