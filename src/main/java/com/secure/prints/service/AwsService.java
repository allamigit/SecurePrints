package com.secure.prints.service;

import com.secure.prints.model.AppointmentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AwsService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String emailFrom;

    public AwsService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendConfirmationEmail(String firstName, String emailTo, AppointmentResponse appointmentResponse) {
        SimpleMailMessage message = new SimpleMailMessage();
        String reason = "";
        if(appointmentResponse.getBciReasonCode() != null) {
            reason = "\nBCI Reason Code: " + appointmentResponse.getBciReasonCode();
            reason = reason + "\nBCI Reason Description: " + appointmentResponse.getBciReasonDescription();
        }
        if(appointmentResponse.getFbiReasonCode() != null) {
            reason = reason + "\nFBI Reason Code: " + appointmentResponse.getFbiReasonCode();
            reason = reason + "\nFBI Reason Description: " + appointmentResponse.getFbiReasonDescription();
        }

        message.setFrom(emailFrom);
        message.setTo(emailTo);
        message.setBcc(emailFrom);
        message.setSubject("SecurePrints: Appointment ID (" + appointmentResponse.getAppointmentId() + ") " + appointmentResponse.getAppointmentStatus());
        message.setText(
                "\n\nHi " + firstName + "," +
                "\n\nAppointment ID: " + appointmentResponse.getAppointmentId() +
                "\nService Name: " + appointmentResponse.getServiceName() +
                reason +
                "\nAppointment Date and Time: " + appointmentResponse.getAppointmentTimestamp() +
                "\nAppointment Status: " + appointmentResponse.getAppointmentStatus() +
                "\n\nKind Regards,\nSecurePrints Team");
        mailSender.send(message);
    }

}
