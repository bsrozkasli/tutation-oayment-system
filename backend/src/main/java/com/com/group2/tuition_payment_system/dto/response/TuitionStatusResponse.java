package com.group2.tuition_payment_system.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TuitionStatusResponse {
    private String studentNo;
    private Double totalTuitionAmount;
    private Double currentBalance;
}
