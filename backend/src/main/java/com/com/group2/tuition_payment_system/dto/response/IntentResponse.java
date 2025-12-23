package com.group2.tuition_payment_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentResponse {
    private String intent; // QUERY_TUITION, PAY_TUITION, UNPAID_TUITION
    private String studentNo;
    private String term;
    private Double amount;
    private String message; // User's original message
}


