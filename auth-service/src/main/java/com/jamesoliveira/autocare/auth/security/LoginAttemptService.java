package com.jamesoliveira.autocare.auth.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic brute-force protection: after 5 wrong passwords for the same email,
 * that email is locked for 5 minutes.
 */
@Service
public class LoginAttemptService {

    static final int MAX_FAILED_ATTEMPTS = 5;
    static final Duration LOCK_TIME = Duration.ofMinutes(5);

    private record Attempts(int failures, Instant lockedUntil) {
    }

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    public boolean isLocked(String email) {
        Attempts current = attempts.get(key(email));
        return current != null
                && current.lockedUntil() != null
                && Instant.now().isBefore(current.lockedUntil());
    }

    public void recordFailure(String email) {
        attempts.compute(key(email), (k, current) -> {
            int failures = (current == null || isExpiredLock(current)) ? 1 : current.failures() + 1;
            Instant lockedUntil = failures >= MAX_FAILED_ATTEMPTS ? Instant.now().plus(LOCK_TIME) : null;
            return new Attempts(failures, lockedUntil);
        });
    }

    public void recordSuccess(String email) {
        attempts.remove(key(email));
    }

    private static boolean isExpiredLock(Attempts a) {
        return a.lockedUntil() != null && Instant.now().isAfter(a.lockedUntil());
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
