package com.jamesoliveira.autocare.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/** Checks the signature and expiry of tokens issued by auth-service. */
@Component
public class JwtValidator {

    private final SecretKey key;

    public JwtValidator(@Value("${app.jwt.secret}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<AuthenticatedUser> validate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String role = claims.get("role", String.class);
            if (claims.getSubject() == null || role == null) {
                return Optional.empty();
            }
            String name = claims.get("name", String.class);
            return Optional.of(new AuthenticatedUser(claims.getSubject(), name == null ? "" : name, role));
        } catch (JwtException | IllegalArgumentException e) {
            // expired, tampered with, signed with another key, or not a JWT at all
            return Optional.empty();
        }
    }
}
