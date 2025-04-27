package com.secure_prints;

import com.secure_prints.service.ReasonService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SecurePrintsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurePrintsApplication.class, args);
		ReasonService.refreshReasonList();
	}

}
