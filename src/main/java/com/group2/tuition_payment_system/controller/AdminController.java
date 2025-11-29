package com.group2.tuition_payment_system.controller;

import com.group2.tuition_payment_system.entity.Tuition;
import com.group2.tuition_payment_system.service.TuitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;


@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin APIs", description = "University Web Site Admin Operations")

@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final TuitionService tuitionService;


    @PostMapping("/tuition")
    @Operation(summary = "Add Single Tuition", description = "Adds a debt record for a student manually.")
    public ResponseEntity<String> addTuition(@RequestParam String studentNo,
                                             @RequestParam String term,
                                             @RequestParam Double amount) {
        tuitionService.addTuition(studentNo, term, amount);
        return ResponseEntity.ok("Tuition added successfully");
    }


    @PostMapping(value = "/tuition/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Add Tuition Batch (CSV)", description = "Uploads a CSV file.")
    public ResponseEntity<String> addTuitionBatch(@RequestParam("file") MultipartFile file) {
        tuitionService.addTuitionBatch(file);
        return ResponseEntity.ok("Batch processing completed successfully");
    }

    @GetMapping("/tuition/unpaid")
    @Operation(summary = "Get Unpaid Tuitions", description = "Lists students with remaining balance (Supports Paging).")
    public ResponseEntity<Page<Tuition>> getUnpaidTuitions(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(tuitionService.getUnpaidTuitions(term, page, size));
    }
}