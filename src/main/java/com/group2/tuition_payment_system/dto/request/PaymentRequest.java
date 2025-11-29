package com.group2.tuition_payment_system.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Student number cannot be null")
    private String studentNo;

    @NotNull(message = "Term cannot be null")
    private String term;

    @NotNull
    @Positive(message = "Payment amount must be positive")
    private Double amount;
}
