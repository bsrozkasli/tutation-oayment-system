package com.group2.tuition_payment_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.tuition_payment_system.dto.response.IntentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Intent Cache - stores parsed intents to avoid repeated API calls
    private final Map<String, CachedIntent> intentCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 60 * 60 * 1000; // 1 hour cache TTL

    // Cache entry class
    private static class CachedIntent {
        IntentResponse intent;
        long timestamp;

        CachedIntent(IntentResponse intent) {
            this.intent = intent;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }

    /**
     * Parse user intent and extract parameters from the message
     * Uses cache to avoid repeated API calls for similar questions
     */
    public IntentResponse parseIntent(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return IntentResponse.builder()
                    .intent("UNKNOWN")
                    .message(userMessage)
                    .build();
        }

        // Normalize message for cache lookup
        String normalizedMessage = normalizeMessage(userMessage);
        String cacheKey = generateCacheKey(normalizedMessage);

        // Check cache first
        CachedIntent cached = intentCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            System.out.println("🔄 Cache HIT - Returning cached intent for: " + userMessage);
            return cached.intent;
        }

        // Clean expired entries periodically
        if (intentCache.size() > 100) {
            cleanExpiredCache();
        }

        System.out.println("🌐 Cache MISS - Calling Gemini API for: " + userMessage);

        try {
            String prompt = buildIntentParsingPrompt(userMessage);
            String response = callGeminiAPI(prompt);
            IntentResponse intentResponse = extractIntentFromResponse(response, userMessage);

            // Store in cache
            intentCache.put(cacheKey, new CachedIntent(intentResponse));
            System.out.println("💾 Cached intent for future use. Cache size: " + intentCache.size());

            return intentResponse;
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: try to extract intent using regex patterns (no API call needed)
            IntentResponse fallback = extractIntentWithRegex(userMessage);
            intentCache.put(cacheKey, new CachedIntent(fallback));
            return fallback;
        }
    }

    /**
     * Normalize message for better cache matching
     */
    private String normalizeMessage(String message) {
        // Convert to lowercase, remove extra spaces, standardize number formats
        return message.toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Generate cache key from normalized message
     * Uses hash to handle long messages efficiently
     */
    private String generateCacheKey(String normalizedMessage) {
        // Extract key parameters for smarter caching
        Pattern studentNoPattern = Pattern.compile("\\b\\d{4,7}\\b");
        Matcher studentMatcher = studentNoPattern.matcher(normalizedMessage);
        String studentNo = studentMatcher.find() ? studentMatcher.group() : "";

        Pattern termPattern = Pattern.compile("\\b\\d{4}-(spring|summer|fall|winter)\\b", Pattern.CASE_INSENSITIVE);
        Matcher termMatcher = termPattern.matcher(normalizedMessage);
        String term = termMatcher.find() ? termMatcher.group() : "";

        // Determine intent type from keywords
        String intentType = "query";
        if (normalizedMessage.contains("pay") || normalizedMessage.contains("payment")) {
            intentType = "pay";
        } else if (normalizedMessage.contains("unpaid") || normalizedMessage.contains("outstanding")) {
            intentType = "unpaid";
        }

        // Create cache key from extracted parameters
        return String.format("%s:%s:%s", intentType, studentNo, term);
    }

    /**
     * Clean expired cache entries
     */
    private void cleanExpiredCache() {
        intentCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        System.out.println("🧹 Cleaned expired cache entries. Remaining: " + intentCache.size());
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", intentCache.size());
        stats.put("cacheTTLMinutes", CACHE_TTL_MS / 60000);
        return stats;
    }

    /**
     * Clear all cache entries
     */
    public void clearCache() {
        intentCache.clear();
        System.out.println("🗑️ Cache cleared");
    }

    /**
     * Build prompt for intent parsing
     */
    private String buildIntentParsingPrompt(String userMessage) {
        return String.format(
                """
                        Analyze the following user message and extract the intent and parameters.
                        The user is interacting with a tuition payment system.

                        Possible intents:
                        1. QUERY_TUITION - User wants to check their tuition balance (requires studentNo)
                        2. PAY_TUITION - User wants to pay tuition (requires studentNo, term, amount)
                        3. UNPAID_TUITION - User wants to see unpaid tuitions (requires term)

                        User message: "%s"

                        IMPORTANT EXTRACTION RULES:
                        - studentNo: A 6-8 digit number like 2023001, 20210001, 2023002. This is NOT a year!
                          Look for phrases like "student 2023001" or "student number 20210001".
                          The number after "student" or "student number" is the studentNo.
                        - term: Format YYYY-SEASON like 2025-SPRING, 2024-FALL, 2025-SUMMER.
                          The YYYY part (e.g., 2025) in a term is NOT a student number!
                        - amount: A payment amount in dollars (e.g., 500, 1000, 500.00)

                        Respond ONLY with this exact JSON format:
                        {
                          "intent": "QUERY_TUITION or PAY_TUITION or UNPAID_TUITION",
                          "studentNo": "the student number as string or null",
                          "term": "the term as string or null",
                          "amount": numeric_amount_or_null
                        }

                        Example 1: "What is tuition for student 2023001?" → studentNo: "2023001", term: null
                        Example 2: "Pay 500 for student 2023001 for 2025-SPRING" → studentNo: "2023001", term: "2025-SPRING", amount: 500
                        Example 3: "Show unpaid for 2025-SPRING" → studentNo: null, term: "2025-SPRING"
                        """,
                userMessage);
    }

    /**
     * Call Gemini API
     */
    private String callGeminiAPI(String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> parts = new HashMap<>();
            parts.put("parts", List.of(textPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(parts));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String finalUrl = API_URL + apiKey;
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.postForEntity(finalUrl, entity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            if (body != null && body.containsKey("candidates")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (!candidates.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> responseParts = (List<Map<String, Object>>) content.get("parts");
                    if (!responseParts.isEmpty()) {
                        return (String) responseParts.get(0).get("text");
                    }
                }
            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Extract intent from Gemini response
     */
    private IntentResponse extractIntentFromResponse(String response, String originalMessage) {
        try {
            // Try to extract JSON from response
            String jsonStr = extractJsonFromResponse(response);
            if (jsonStr != null && !jsonStr.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = objectMapper.readValue(jsonStr, Map.class);

                return IntentResponse.builder()
                        .intent((String) parsed.getOrDefault("intent", "QUERY_TUITION"))
                        .studentNo((String) parsed.get("studentNo"))
                        .term((String) parsed.get("term"))
                        .amount(parsed.get("amount") != null ? Double.parseDouble(parsed.get("amount").toString())
                                : null)
                        .message(originalMessage)
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallback to regex extraction
        return extractIntentWithRegex(originalMessage);
    }

    /**
     * Extract JSON from response text
     */
    private String extractJsonFromResponse(String response) {
        // Try to find JSON object in response
        Pattern jsonPattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(response);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * Fallback: Extract intent using regex patterns (NO API CALL - FREE!)
     */
    private IntentResponse extractIntentWithRegex(String message) {
        String lowerMessage = message.toLowerCase();

        // Determine intent
        String intent = "QUERY_TUITION"; // default
        if (lowerMessage.contains("pay") || lowerMessage.contains("payment") ||
                lowerMessage.contains("ödeme") || lowerMessage.contains("öde")) {
            intent = "PAY_TUITION";
        } else if (lowerMessage.contains("unpaid") || lowerMessage.contains("outstanding") ||
                lowerMessage.contains("ödenmemiş") || lowerMessage.contains("borç")) {
            intent = "UNPAID_TUITION";
        }

        // Extract term FIRST (pattern: YYYY-SEASON) - so we can exclude it from student
        // number search
        Pattern termPattern = Pattern.compile("\\b(\\d{4})-(SPRING|SUMMER|FALL|WINTER|SPR|SUM|FAL|WIN)\\b",
                Pattern.CASE_INSENSITIVE);
        Matcher termMatcher = termPattern.matcher(message);
        String term = termMatcher.find() ? termMatcher.group().toUpperCase() : null;

        // Remove term from message before extracting student number to avoid confusion
        String messageWithoutTerm = message;
        if (term != null) {
            messageWithoutTerm = message.replaceAll("(?i)\\b\\d{4}-(SPRING|SUMMER|FALL|WINTER|SPR|SUM|FAL|WIN)\\b", "");
        }

        // Extract student number (pattern: 6-8 digit number typically, or "student"
        // followed by number)
        // First try explicit "student" + number pattern
        Pattern explicitStudentPattern = Pattern.compile(
                "(?:student|öğrenci)\\s*(?:no|number|numarası|id)?[:\\s]*(\\d{4,8})", Pattern.CASE_INSENSITIVE);
        Matcher explicitMatcher = explicitStudentPattern.matcher(message);
        String studentNo = null;

        if (explicitMatcher.find()) {
            studentNo = explicitMatcher.group(1);
        } else {
            // Fallback: look for 6-8 digit numbers (more likely to be student IDs, not
            // years)
            Pattern studentNoPattern = Pattern.compile("\\b(\\d{6,8})\\b");
            Matcher studentMatcher = studentNoPattern.matcher(messageWithoutTerm);
            if (studentMatcher.find()) {
                studentNo = studentMatcher.group(1);
            } else {
                // Last resort: 4-7 digit numbers but NOT common years (2020-2030 range)
                Pattern shortStudentPattern = Pattern.compile("\\b(\\d{4,7})\\b");
                Matcher shortMatcher = shortStudentPattern.matcher(messageWithoutTerm);
                while (shortMatcher.find()) {
                    String candidate = shortMatcher.group(1);
                    int num = Integer.parseInt(candidate);
                    // Exclude years 2020-2030 and very small numbers
                    if (num < 2020 || num > 2030) {
                        studentNo = candidate;
                        break;
                    }
                }
            }
        }

        // Extract amount - look for currency patterns or "pay X", "amount X"
        Double amount = null;
        // Try explicit amount patterns first
        Pattern explicitAmountPattern = Pattern.compile("(?:pay|amount|ödeme|miktar|\\$)\\s*(\\d+(?:\\.\\d{2})?)",
                Pattern.CASE_INSENSITIVE);
        Matcher explicitAmountMatcher = explicitAmountPattern.matcher(message);
        if (explicitAmountMatcher.find()) {
            try {
                amount = Double.parseDouble(explicitAmountMatcher.group(1));
            } catch (NumberFormatException e) {
                // Ignore
            }
        } else {
            // Look for currency symbols
            Pattern currencyPattern = Pattern.compile(
                    "\\$\\s*(\\d+(?:\\.\\d{2})?)|(\\d+(?:\\.\\d{2})?)\\s*(?:dollars?|lira|tl)",
                    Pattern.CASE_INSENSITIVE);
            Matcher currencyMatcher = currencyPattern.matcher(message);
            if (currencyMatcher.find()) {
                try {
                    String amountStr = currencyMatcher.group(1) != null ? currencyMatcher.group(1)
                            : currencyMatcher.group(2);
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }

        return IntentResponse.builder()
                .intent(intent)
                .studentNo(studentNo)
                .term(term)
                .amount(amount)
                .message(message)
                .build();
    }

    /**
     * Generate a natural language response from API result
     */
    public String generateResponse(String intent, Object apiResult) {
        try {
            String prompt = String.format("""
                    The user asked about: %s
                    The API returned: %s

                    Generate a friendly, natural language response explaining the result to the user.
                    Keep it concise and helpful.
                    """, intent, apiResult.toString());

            return callGeminiAPI(prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return "I've processed your request. " + apiResult.toString();
        }
    }
}