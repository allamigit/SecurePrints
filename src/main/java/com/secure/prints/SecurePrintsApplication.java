package com.secure.prints;

import com.secure.prints.service.AppointmentPaymentService;
import com.secure.prints.service.CompanyService;
import com.secure.prints.service.ReasonService;
import com.secure.prints.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SecurePrintsApplication {

	/**
	 * Main method for secure-prints project
	 * @param args arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(SecurePrintsApplication.class, args);
		CompanyService.getCompanyDetails(1);
		UserService.getAllUsers();
		ReasonService.refreshReasonList();
		AppointmentPaymentService.generateExpenseTypeList();
	}

}
