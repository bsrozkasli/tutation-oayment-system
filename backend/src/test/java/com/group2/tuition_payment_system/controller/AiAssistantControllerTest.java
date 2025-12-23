package com.group2.tuition_payment_system.controller;

import com.group2.tuition_payment_system.dto.response.IntentResponse;
import com.group2.tuition_payment_system.service.GeminiService;
import com.group2.tuition_payment_system.service.TuitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AiAssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GeminiService geminiService;

    @MockBean
    private TuitionService tuitionService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
    }

    @Test
    @DisplayName("POST /api/v1/ai/chat - Empty message returns 400")
    void chat_emptyMessage_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Message cannot be empty"));
    }

    @Test
    @DisplayName("POST /api/v1/ai/chat - Null message returns 400")
    void chat_nullMessage_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Message cannot be empty"));
    }

    @Test
    @DisplayName("POST /api/v1/ai/chat - Valid query tuition intent")
    void chat_queryTuitionIntent_returnsResponse() throws Exception {
        IntentResponse intentResponse = new IntentResponse();
        intentResponse.setIntent("QUERY_TUITION");
        intentResponse.setStudentNo("2023001");

        when(geminiService.parseIntent(anyString())).thenReturn(intentResponse);

        mockMvc.perform(post("/api/v1/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Check tuition for student 2023001\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/ai/chat - Unknown intent returns help message")
    void chat_unknownIntent_returnsHelpMessage() throws Exception {
        IntentResponse intentResponse = new IntentResponse();
        intentResponse.setIntent("UNKNOWN");

        when(geminiService.parseIntent(anyString())).thenReturn(intentResponse);

        mockMvc.perform(post("/api/v1/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("student number")));
    }

    @Test
    @DisplayName("POST /api/v1/ai/chat - Query tuition without student number")
    void chat_queryTuitionNoStudentNo_asksForStudentNo() throws Exception {
        IntentResponse intentResponse = new IntentResponse();
        intentResponse.setIntent("QUERY_TUITION");
        intentResponse.setStudentNo(null);

        when(geminiService.parseIntent(anyString())).thenReturn(intentResponse);

        mockMvc.perform(post("/api/v1/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Check my tuition\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("student number")));
    }

    @Test
    @DisplayName("POST /api/v1/ai/chat - Pay tuition missing parameters")
    void chat_payTuitionMissingParams_asksForParams() throws Exception {
        IntentResponse intentResponse = new IntentResponse();
        intentResponse.setIntent("PAY_TUITION");
        intentResponse.setStudentNo("2023001");
        intentResponse.setTerm(null);
        intentResponse.setAmount(null);

        when(geminiService.parseIntent(anyString())).thenReturn(intentResponse);

        mockMvc.perform(post("/api/v1/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"I want to pay tuition\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("term")));
    }
}
