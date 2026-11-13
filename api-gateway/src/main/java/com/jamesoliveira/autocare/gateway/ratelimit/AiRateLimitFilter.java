package com.jamesoliveira.autocare.gateway.ratelimit;

import com.jamesoliveira.autocare.gateway.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

/**
 * Every AI answer costs money, so the assistant is limited per user
 * (or per IP address for visitors who aren't logged in).
 */
@Component
public class AiRateLimitFilter implements GlobalFilter, Ordered {

    private final SlidingWindowRateLimiter limiter;

    public AiRateLimitFilter(@Value("${app.rate-limit.ai.max-requests:10}") int maxRequests,
                             @Value("${app.rate-limit.ai.window-seconds:60}") long windowSeconds) {
        this.limiter = new SlidingWindowRateLimiter(maxRequests, Duration.ofSeconds(windowSeconds), Clock.systemUTC());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (!request.getPath().value().startsWith("/api/ai/diagnose")) {
            return chain.filter(exchange);
        }
        if (limiter.tryAcquire(clientKey(request))) {
            return chain.filter(exchange);
        }
        return tooManyRequests(exchange.getResponse());
    }

    /** Runs after JwtAuthenticationFilter, so a logged-in user's id is already in the headers. */
    @Override
    public int getOrder() {
        return -90;
    }

    private static String clientKey(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst(JwtAuthenticationFilter.USER_ID);
        if (userId != null) {
            return "user:" + userId;
        }
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return "ip:" + forwardedFor.split(",")[0].trim();
        }
        InetSocketAddress remote = request.getRemoteAddress();
        if (remote != null && remote.getAddress() != null) {
            return "ip:" + remote.getAddress().getHostAddress();
        }
        return "ip:unknown";
    }

    private Mono<Void> tooManyRequests(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("Retry-After", String.valueOf(limiter.windowSeconds()));
        byte[] body = "{\"error\":\"You're asking a lot of questions! Please wait a minute and try again.\"}"
                .getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }
}
