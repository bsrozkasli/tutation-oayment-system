package com.group2.tuition_payment_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Tuition Payment System.
 * This is a REST API application that handles tuition fee queries and payments
 * for university students. It provides endpoints for mobile apps, banking apps, and admin operations.
 *
 * @author Group 2
 * @version 1.0
 */
@SpringBootApplication
public class TuitionPaymentSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(TuitionPaymentSystemApplication.class, args);
	}

}
