package com.group2.tuition_payment_system.config;

import com.group2.tuition_payment_system.dto.request.PaymentRequest;
import com.group2.tuition_payment_system.dto.response.TuitionStatusResponse;
import com.group2.tuition_payment_system.service.TuitionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import java.util.function.Function;

@Configuration
public class AiAgentConfig {

    // Borç Sorgulama
    @Bean
    @Description("Queries the tuition balance of a student given their student number (studentNo).")
    public Function<TuitionQueryRequest, TuitionStatusResponse> checkTuitionStatus(TuitionService tuitionService) {
        return request -> tuitionService.queryTuition(request.studentNo());
    }

    //  Ödeme Yapma
    @Bean
    @Description("Processes a tuition payment for a student. Requires studentNo, term, and amount.")
    public Function<PaymentRequest, String> makePayment(TuitionService tuitionService) {
        return request -> {
            tuitionService.payTuition(request);
            return "Payment processed successfully.";
        };
    }

    // AI için girdi modeli
    public record TuitionQueryRequest(String studentNo) {}
}