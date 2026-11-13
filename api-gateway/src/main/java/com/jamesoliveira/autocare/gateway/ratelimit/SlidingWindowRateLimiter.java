package com.jamesoliveira.autocare.gateway.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allows at most maxRequests per key (user id or IP address) in any rolling time window.
 * Kept in memory, which is fine for a single gateway instance; with several instances
 * this would move to Redis.
 */
public class SlidingWindowRateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final Clock clock;
    private final Map<String, Deque<Long>> requests = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequests, Duration window, Clock clock) {
        this.maxRequests = maxRequests;
        this.windowMillis = window.toMillis();
        this.clock = clock;
    }

    public boolean tryAcquire(String key) {
        long now = clock.millis();
        Deque<Long> timestamps = requests.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= windowMillis) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequests) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    public long windowSeconds() {
        return Math.max(1, windowMillis / 1000);
    }
}
