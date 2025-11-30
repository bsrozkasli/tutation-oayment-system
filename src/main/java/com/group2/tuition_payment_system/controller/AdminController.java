package com.group2.tuition_payment_system.controller;

import com.group2.tuition_payment_system.dto.request.TuitionAddRequest;
import com.group2.tuition_payment_system.entity.Tuition;
import com.group2.tuition_payment_system.service.TuitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for admin operations.
 * Handles administrative tasks such as adding tuition records,
 * batch uploading CSV files, and querying unpaid tuition status.
 * All endpoints require ADMIN role authentication.
 *
 * @author Group 2
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin APIs", description = "Admin Operations (Auth Required)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final TuitionService tuitionService;


    @PostMapping("/add-tuition")
    @Operation(summary = "Add Single Tuition", description = "Adds a tuition amount for a specific student and term.")
    public ResponseEntity<String> addTuition(@RequestBody TuitionAddRequest request) {
        tuitionService.addTuition(request.getStudentNo(), request.getTerm(), request.getAmount());
        return ResponseEntity.ok("Tuition added successfully.");
    }


    @PostMapping(value = "/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Batch Upload", description = "Uploads CSV file for bulk tuition entry. CSV format: studentNo,term,amount")
    public ResponseEntity<String> uploadBatch(@RequestParam("file") MultipartFile file) {
        try {
            int processedCount = tuitionService.processBatchUpload(file);
            return ResponseEntity.ok("Batch upload processed successfully. " + processedCount + " records added.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing batch upload: " + e.getMessage());
        }
    }


    @GetMapping("/unpaid-status")
    @Operation(summary = "Unpaid Tuition Status", description = "Lists students with unpaid tuition for a term with paging.")
    public ResponseEntity<Page<Tuition>> getUnpaidTuitions(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(tuitionService.getUnpaidTuitions(term, pageable));
    }
}