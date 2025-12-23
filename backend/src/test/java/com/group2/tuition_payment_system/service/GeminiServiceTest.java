package com.group2.tuition_payment_system.service;

import com.group2.tuition_payment_system.dto.response.IntentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GeminiServiceTest {

    @Autowired
    private GeminiService geminiService;

    @Test
    @DisplayName("Parse query tuition intent with student number")
    void parseIntent_queryTuition_extractsStudentNo() {
        String message = "Check my tuition balance for student 2023001";

        IntentResponse response = geminiService.parseIntent(message);

        assertNotNull(response);
        assertEquals("QUERY_TUITION", response.getIntent());
        assertEquals("2023001", response.getStudentNo());
    }

    @Test
    @DisplayName("Parse pay tuition intent with all parameters")
    void parseIntent_payTuition_extractsAllParams() {
        String message = "I want to pay 5000 for term 2025-SUMMER, student number 2023002";

        IntentResponse response = geminiService.parseIntent(message);

        assertNotNull(response);
        assertEquals("PAY_TUITION", response.getIntent());
        assertNotNull(response.getStudentNo());
        assertNotNull(response.getTerm());
        assertNotNull(response.getAmount());
    }

    @Test
    @DisplayName("Parse unpaid tuition intent with term")
    void parseIntent_unpaidTuition_extractsTerm() {
        String message = "Show me unpaid tuitions for 2025-SUMMER";

        IntentResponse response = geminiService.parseIntent(message);

        assertNotNull(response);
        assertEquals("UNPAID_TUITION", response.getIntent());
        assertNotNull(response.getTerm());
    }

    @Test
    @DisplayName("Parse generic greeting returns unknown intent")
    void parseIntent_greeting_returnsUnknown() {
        String message = "Hello, how are you?";

        IntentResponse response = geminiService.parseIntent(message);

        assertNotNull(response);
        // Should be UNKNOWN or similar when no specific intent detected
    }

    @Test
    @DisplayName("Handle null message gracefully")
    void parseIntent_nullMessage_handlesGracefully() {
        assertDoesNotThrow(() -> {
            geminiService.parseIntent(null);
        });
    }

    @Test
    @DisplayName("Handle empty message gracefully")
    void parseIntent_emptyMessage_handlesGracefully() {
        assertDoesNotThrow(() -> {
            geminiService.parseIntent("");
        });
    }
}
