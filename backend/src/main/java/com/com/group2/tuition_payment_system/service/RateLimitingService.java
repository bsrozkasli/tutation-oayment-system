package com.group2.tuition_payment_system.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service class for rate limiting functionality.
 * Implements rate limiting logic to restrict API requests to 3 per student per day.
 * Uses in-memory storage to track request timestamps for each student.
 *
 * @author Group 2
 */
@Service
public class RateLimitingService {

    private final Map<String, List<Long>> requestMap = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 3;

    /**
     * Checks if a student is allowed to make a request (3 requests per student per day)
     * @param studentNo Student number
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean isAllowedForStudent(String studentNo) {
        long currentTime = System.currentTimeMillis();
        long todayStart = getTodayStartTimestamp();

        requestMap.putIfAbsent(studentNo, new CopyOnWriteArrayList<>());
        List<Long> timestamps = requestMap.get(studentNo);

        synchronized (timestamps) {

            timestamps.removeIf(time -> time < todayStart);


            if (timestamps.size() < MAX_REQUESTS) {
                timestamps.add(currentTime);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Gets the timestamp for the start of today (00:00:00)
     */
    private long getTodayStartTimestamp() {
        LocalDate today = LocalDate.now();
        return today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}