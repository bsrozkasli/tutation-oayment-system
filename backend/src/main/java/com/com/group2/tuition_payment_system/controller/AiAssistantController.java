package com.group2.tuition_payment_system.controller;

import com.group2.tuition_payment_system.dto.request.PaymentRequest;
import com.group2.tuition_payment_system.dto.response.IntentResponse;
import com.group2.tuition_payment_system.dto.response.TuitionStatusResponse;
import com.group2.tuition_payment_system.service.GeminiService;
import com.group2.tuition_payment_system.service.TuitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final GeminiService geminiService;
    private final TuitionService tuitionService;

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Message cannot be empty");
        }

        try {
            // Parse intent and extract parameters
            IntentResponse intentResponse = geminiService.parseIntent(message);

            // Call appropriate API based on intent
            String response = handleIntent(intentResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("I'm sorry, I encountered an error processing your request: " + e.getMessage());
        }
    }

    /**
     * Debug endpoint - test intent parsing without calling APIs
     */
    @PostMapping("/debug")
    public ResponseEntity<IntentResponse> debugIntent(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        IntentResponse intentResponse = geminiService.parseIntent(message);
        return ResponseEntity.ok(intentResponse);
    }

    /**
     * Clear intent cache
     */
    @DeleteMapping("/cache")
    public ResponseEntity<String> clearCache() {
        geminiService.clearCache();
        return ResponseEntity.ok("Cache cleared successfully");
    }

    /**
     * Get cache statistics
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        return ResponseEntity.ok(geminiService.getCacheStats());
    }

    /**
     * Handle different intents and call appropriate APIs
     */
    private String handleIntent(IntentResponse intentResponse) {
        try {
            switch (intentResponse.getIntent()) {
                case "QUERY_TUITION":
                    return handleQueryTuition(intentResponse);

                case "PAY_TUITION":
                    return handlePayTuition(intentResponse);

                case "UNPAID_TUITION":
                    return handleUnpaidTuition(intentResponse);

                default:
                    return "I understand you're asking about tuition. Could you please provide your student number? " +
                            "You can ask me to: check your tuition balance, pay tuition, or view unpaid tuitions.";
            }
        } catch (Exception e) {
            return "I encountered an error: " + e.getMessage() + ". Please try again with more specific information.";
        }
    }

    /**
     * Handle query tuition intent
     */
    private String handleQueryTuition(IntentResponse intentResponse) {
        if (intentResponse.getStudentNo() == null || intentResponse.getStudentNo().trim().isEmpty()) {
            return "To check your tuition balance, I need your student number. Please provide it in your message.";
        }

        try {
            TuitionStatusResponse status = tuitionService.queryTuition(intentResponse.getStudentNo());

            return String.format(
                    "Tuition Status for Student %s:\n" +
                            "Total Tuition Amount: $%.2f\n" +
                            "Current Balance: $%.2f\n" +
                            "%s",
                    status.getStudentNo(),
                    status.getTotalTuitionAmount(),
                    status.getCurrentBalance(),
                    status.getCurrentBalance() > 0
                            ? "You have an outstanding balance. Would you like to make a payment?"
                            : "Great! You have no outstanding balance.");
        } catch (Exception e) {
            return "I couldn't retrieve your tuition information. Error: " + e.getMessage() +
                    ". Please verify your student number and try again.";
        }
    }

    /**
     * Handle pay tuition intent
     */
    private String handlePayTuition(IntentResponse intentResponse) {
        if (intentResponse.getStudentNo() == null || intentResponse.getStudentNo().trim().isEmpty()) {
            return "To process a payment, I need your student number. Please provide it.";
        }

        if (intentResponse.getTerm() == null || intentResponse.getTerm().trim().isEmpty()) {
            return "To process a payment, I need the term (e.g., 2025-SUMMER). Please provide it.";
        }

        if (intentResponse.getAmount() == null || intentResponse.getAmount() <= 0) {
            return "To process a payment, I need the payment amount. Please specify how much you want to pay.";
        }

        try {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setStudentNo(intentResponse.getStudentNo());
            paymentRequest.setTerm(intentResponse.getTerm());
            paymentRequest.setAmount(intentResponse.getAmount());

            tuitionService.payTuition(paymentRequest);

            return String.format(
                    "Payment processed successfully!\n" +
                            "Student: %s\n" +
                            "Term: %s\n" +
                            "Amount Paid: $%.2f\n" +
                            "Your payment has been recorded.",
                    intentResponse.getStudentNo(),
                    intentResponse.getTerm(),
                    intentResponse.getAmount());
        } catch (Exception e) {
            return "I couldn't process your payment. Error: " + e.getMessage() +
                    ". Please verify your student number, term, and amount, then try again.";
        }
    }

    /**
     * Handle unpaid tuition intent
     */
    private String handleUnpaidTuition(IntentResponse intentResponse) {
        if (intentResponse.getTerm() == null || intentResponse.getTerm().trim().isEmpty()) {
            return "To view unpaid tuitions, I need the term (e.g., 2025-SUMMER). Please provide it.";
        }

        try {
            Pageable pageable = PageRequest.of(0, 10);
            Page<com.group2.tuition_payment_system.entity.Tuition> unpaidTuitions = tuitionService
                    .getUnpaidTuitions(intentResponse.getTerm(), pageable);

            if (unpaidTuitions.isEmpty()) {
                return String.format("No unpaid tuitions found for term: %s", intentResponse.getTerm());
            }

            StringBuilder response = new StringBuilder();
            response.append(String.format("Unpaid Tuitions for Term: %s\n\n", intentResponse.getTerm()));

            unpaidTuitions.getContent().forEach(tuition -> {
                response.append(String.format(
                        "Student: %s | Amount: $%.2f | Balance: $%.2f\n",
                        tuition.getStudent().getStudentNo(),
                        tuition.getAmount(),
                        tuition.getBalance()));
            });

            response.append(String.format("\nTotal records: %d", unpaidTuitions.getTotalElements()));

            return response.toString();
        } catch (Exception e) {
            return "I couldn't retrieve unpaid tuitions. Error: " + e.getMessage() +
                    ". Please verify the term and try again.";
        }
    }
}