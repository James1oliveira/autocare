package com.jamesoliveira.autocare.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Runs on every request before it is routed to a service.
 *
 * 1. Removes any X-User-* headers sent by the client, so nobody can pretend to be another user.
 * 2. If there is a valid "Authorization: Bearer <jwt>" header, adds X-User-Id, X-User-Role
 *    and X-User-Name for the services behind the gateway.
 * 3. Rejects requests to protected paths that have no valid token (401).
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    public static final String USER_ID = "X-User-Id";
    public static final String USER_ROLE = "X-User-Role";
    public static final String USER_NAME = "X-User-Name";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/catalog",
            "/api/ai/",
            "/actuator/health");

    private final JwtValidator jwtValidator;

    public JwtAuthenticationFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        Optional<AuthenticatedUser> user = bearerToken(request).flatMap(jwtValidator::validate);

        boolean preflight = HttpMethod.OPTIONS.equals(request.getMethod());
        if (user.isEmpty() && !preflight && !isPublic(path)) {
            return reject(exchange.getResponse());
        }

        ServerHttpRequest forwarded = request.mutate()
                .headers(headers -> {
                    headers.remove(USER_ID);
                    headers.remove(USER_ROLE);
                    headers.remove(USER_NAME);
                    user.ifPresent(u -> {
                        headers.set(USER_ID, u.id());
                        headers.set(USER_ROLE, u.role());
                        // Encoded because HTTP headers can't safely carry characters like é or ü
                        headers.set(USER_NAME, URLEncoder.encode(u.name(), StandardCharsets.UTF_8));
                    });
                })
                .build();
        return chain.filter(exchange.mutate().request(forwarded).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    static boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private static Optional<String> bearerToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return Optional.of(header.substring("Bearer ".length()).trim());
        }
        return Optional.empty();
    }

    private static Mono<Void> reject(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = "{\"error\":\"Please log in first\"}".getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }
}
