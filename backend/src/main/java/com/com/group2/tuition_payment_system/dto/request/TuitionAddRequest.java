package com.group2.tuition_payment_system.dto.request;

import lombok.Data;

@Data
public class TuitionAddRequest {
    private String studentNo;
    private String term;
    private Double amount;
}