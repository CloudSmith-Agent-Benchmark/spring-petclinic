package org.springframework.samples.petclinic.system;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RateLimiterConfig {
    private static final int MAX_REQUESTS = 10;  // Maximum requests allowed
    private static final Duration WINDOW_SIZE = Duration.ofSeconds(1);  // Time window of 1 second

    private final Map<String, RequestWindow> requestWindows = new ConcurrentHashMap<>();

    private static class RequestWindow {
        final long startTime;
        final AtomicInteger count;

        RequestWindow() {
            this.startTime = System.currentTimeMillis();
            this.count = new AtomicInteger(0);
        }

        boolean isExpired() {
            return System.currentTimeMillis() - startTime > WINDOW_SIZE.toMillis();
        }
    }

    public void checkRateLimit(String endpoint) {
        String key = endpoint;
        RequestWindow window = requestWindows.compute(key, (k, v) -> {
            if (v == null || v.isExpired()) {
                return new RequestWindow();
            }
            return v;
        });

        if (window.count.incrementAndGet() > MAX_REQUESTS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
        }
    }
}