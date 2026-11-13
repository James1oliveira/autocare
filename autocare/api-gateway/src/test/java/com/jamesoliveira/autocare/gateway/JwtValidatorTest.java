package com.jamesoliveira.autocare.gateway;

import com.jamesoliveira.autocare.gateway.security.AuthenticatedUser;
import com.jamesoliveira.autocare.gateway.security.JwtValidator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtValidatorTest {

    static final String SECRET = "a-test-secret-that-is-long-enough-123456";

    static String token(String secret, Instant expiresAt) {
        return Jwts.builder()
                .subject("7")
                .claim("name", "Emma Botha")
                .claim("role", "CUSTOMER")
                .issuedAt(Date.from(Instant.now().minusSeconds(60)))
                .expiration(Date.from(expiresAt))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private final JwtValidator validator = new JwtValidator(SECRET);

    @Test
    void acceptsValidToken() {
        Optional<AuthenticatedUser> user = validator.validate(token(SECRET, Instant.now().plusSeconds(600)));

        assertThat(user).isPresent();
        assertThat(user.get().id()).isEqualTo("7");
        assertThat(user.get().role()).isEqualTo("CUSTOMER");
        assertThat(user.get().name()).isEqualTo("Emma Botha");
    }

    @Test
    void rejectsExpiredToken() {
        assertThat(validator.validate(token(SECRET, Instant.now().minusSeconds(5)))).isEmpty();
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String forged = token("someone-elses-secret-that-is-also-long-1234", Instant.now().plusSeconds(600));
        assertThat(validator.validate(forged)).isEmpty();
    }

    @Test
    void rejectsTamperedToken() {
        String valid = token(SECRET, Instant.now().plusSeconds(600));
        String tampered = valid.substring(0, valid.length() - 3) + "abc";
        assertThat(validator.validate(tampered)).isEmpty();
    }

    @Test
    void rejectsGarbage() {
        assertThat(validator.validate("not-a-jwt")).isEmpty();
        assertThat(validator.validate("")).isEmpty();
    }
}
