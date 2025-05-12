package com.secure.prints.service;

import com.secure.prints.database.AppointmentInformationRepository;
import com.secure.prints.database.AppointmentPaymentRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class CleanupService {

    private final AppointmentInformationRepository appointmentInformationRepository;
    private final AppointmentPaymentRepository appointmentPaymentRepository;

    /**
     * Constructor for CleanupService
     * @param appointmentInformationRepository appointmentInformationRepository
     * @param appointmentPaymentRepository appointmentPaymentRepository
     */
    public CleanupService(AppointmentInformationRepository appointmentInformationRepository, AppointmentPaymentRepository appointmentPaymentRepository) {
        this.appointmentInformationRepository = appointmentInformationRepository;
        this.appointmentPaymentRepository = appointmentPaymentRepository;
    }

    /**
     * Cleanup Cancelled appointments and payments every day at midnight
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanup() {
        appointmentInformationRepository.cleanupCancelledAppointments();
        appointmentPaymentRepository.cleanupCancelledPayments();
    }

}
