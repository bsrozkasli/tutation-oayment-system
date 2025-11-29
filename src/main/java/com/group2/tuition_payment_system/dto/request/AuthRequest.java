package com.group2.tuition_payment_system.dto.request;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
