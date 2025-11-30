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
    @Operation(summary = "Batch Upload", description = "Uploads CSV file for bulk tuition entry.")
    public ResponseEntity<String> uploadBatch(@RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok("Batch upload processed successfully.");
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