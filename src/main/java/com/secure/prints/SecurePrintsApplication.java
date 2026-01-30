package com.secure.prints;

import com.secure.prints.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
public class SecurePrintsApplication {

	/**
	 * Main method for secure-prints project
	 * @param args arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(SecurePrintsApplication.class, args);
		CompanyService.getCompanyDetails(1);
		ReasonService.refreshReasonList();
		ExpenseTypeService.generateExpenseTypeList();
	}

}
