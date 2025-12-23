package com.group2.tuition_payment_system.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Request/Response logging filter.
 * Logs all HTTP requests and responses including method, path, IP address,
 * headers, status codes, latency, and response sizes.
 * Excludes static resources and Swagger UI from logging.
 *
 * @author Group 2
 */
@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String requestId = String.valueOf(System.currentTimeMillis());


        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long latency = System.currentTimeMillis() - startTime;
            logRequest(wrappedRequest, requestId);
            logResponse(wrappedRequest, wrappedResponse, requestId, latency);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String requestId) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }

        String queryString = request.getQueryString();
        String fullPath = request.getRequestURI() + (queryString != null ? "?" + queryString : "");
        int requestSize = request.getContentLength();

        log.info("REQUEST [{}] - Method: {}, Path: {}, IP: {}, Timestamp: {}, Headers: {}, Request Size: {} bytes",
                requestId,
                request.getMethod(),
                fullPath,
                getClientIpAddress(request),
                System.currentTimeMillis(),
                headers,
                requestSize > 0 ? requestSize : 0);


        String authHeader = request.getHeader("Authorization");
        boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");
        log.info("REQUEST [{}] - Authentication: {}", requestId, isAuthenticated ? "SUCCESS" : "NONE/FAILED");
    }

    private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                            String requestId, long latency) {
        int statusCode = response.getStatus();
        int responseSize = response.getContentSize();

        log.info("RESPONSE [{}] - Status: {}, Latency: {} ms, Response Size: {} bytes",
                requestId,
                statusCode,
                latency,
                responseSize);


        if (statusCode >= 400) {
            log.warn("RESPONSE [{}] - Error Status: {}, Path: {}", requestId, statusCode, request.getRequestURI());
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Don't log static resources and swagger
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui.html")
                || path.startsWith("/webjars")
                || path.startsWith("/favicon.ico");
    }
}

