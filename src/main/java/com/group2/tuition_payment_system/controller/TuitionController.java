package com.group2.tuition_payment_system.controller;

import com.group2.tuition_payment_system.dto.request.PaymentRequest;
import com.group2.tuition_payment_system.dto.response.TuitionStatusResponse;
import com.group2.tuition_payment_system.service.TuitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Public APIs", description = "Mobile and Banking App Operations")
public class TuitionController {

    private final TuitionService tuitionService;

    @GetMapping("/tuition/{studentNo}")
    @Operation(summary = "Query Tuition Status", description = "Returns total tuition and current balance.")
    public ResponseEntity<TuitionStatusResponse> queryTuition(@PathVariable String studentNo) {
        return ResponseEntity.ok(tuitionService.queryTuition(studentNo));
    }

    @PostMapping("/payment")
    @Operation(summary = "Pay Tuition", description = "Process a payment for a specific term.")
    public ResponseEntity<String> payTuition(@Valid @RequestBody PaymentRequest request) {
        tuitionService.payTuition(request);
        return ResponseEntity.ok("Payment Successful");
    }
}