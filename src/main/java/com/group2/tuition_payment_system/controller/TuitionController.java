package com.group2.tuition_payment_system.controller;

import com.group2.tuition_payment_system.dto.request.PaymentRequest;
import com.group2.tuition_payment_system.service.TuitionService;

import com.group2.tuition_payment_system.service.RateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for public tuition and payment operations.
 * Handles requests from mobile apps and banking apps.
 * Provides endpoints for querying tuition status and processing payments.
 *
 * @author Group 2
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Public APIs", description = "Mobile and Banking App Operations")
public class TuitionController {

    private final TuitionService tuitionService;

    private final RateLimitingService rateLimitingService;

    @GetMapping("/tuition/{studentNo}")
    @Operation(summary = "Query Tuition Status (Mobile App)", description = "Returns total tuition and current balance. (Rate Limited: 3 req per student per day)")
    public ResponseEntity<?> queryTuition(@PathVariable String studentNo, HttpServletRequest request) {

        if (!rateLimitingService.isAllowedForStudent(studentNo)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded. You can only make 3 requests per student per day.");
        }

        return ResponseEntity.ok(tuitionService.queryTuition(studentNo));
    }

    @GetMapping("/banking/tuition/{studentNo}")
    @Operation(summary = "Query Tuition Status (Banking App)", description = "Returns total tuition and current balance. Requires authentication.")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> queryTuitionBanking(@PathVariable String studentNo) {
        return ResponseEntity.ok(tuitionService.queryTuition(studentNo));
    }

    @PostMapping("/payment")
    @Operation(summary = "Pay Tuition", description = "Process a payment for a specific term.")
    public ResponseEntity<?> payTuition(@Valid @RequestBody PaymentRequest request) {
        try {
            tuitionService.payTuition(request);
            return ResponseEntity.ok("Payment Successful");
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = e.getClass().getSimpleName() + " occurred";
            }
            return ResponseEntity.badRequest().body("Payment failed: " + errorMessage);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = e.getClass().getSimpleName() + " occurred";
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Payment failed: " + errorMessage);
        }
    }
}