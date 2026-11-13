package com.jamesoliveira.autocare.gateway;

import com.jamesoliveira.autocare.gateway.ratelimit.SlidingWindowRateLimiter;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class SlidingWindowRateLimiterTest {

    /** A clock the test can move forward by hand. */
    static class TestClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T10:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    @Test
    void blocksAfterLimitAndRecoversAfterWindow() {
        TestClock clock = new TestClock();
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(3, Duration.ofSeconds(60), clock);

        assertThat(limiter.tryAcquire("user:1")).isTrue();
        assertThat(limiter.tryAcquire("user:1")).isTrue();
        assertThat(limiter.tryAcquire("user:1")).isTrue();
        assertThat(limiter.tryAcquire("user:1")).isFalse();

        clock.advance(Duration.ofSeconds(61));
        assertThat(limiter.tryAcquire("user:1")).isTrue();
    }

    @Test
    void eachUserHasTheirOwnLimit() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(1, Duration.ofSeconds(60), new TestClock());

        assertThat(limiter.tryAcquire("user:1")).isTrue();
        assertThat(limiter.tryAcquire("user:1")).isFalse();
        assertThat(limiter.tryAcquire("user:2")).isTrue();
    }
}
