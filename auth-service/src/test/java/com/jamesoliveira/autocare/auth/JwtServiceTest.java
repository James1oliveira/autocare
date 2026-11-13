package com.jamesoliveira.autocare.auth;

import com.jamesoliveira.autocare.auth.security.JwtService;
import com.jamesoliveira.autocare.auth.user.Role;
import com.jamesoliveira.autocare.auth.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "a-test-secret-that-is-long-enough-123456";

    @Test
    void tokenContainsUserDetails() {
        JwtService jwtService = new JwtService(SECRET, 60);
        User user = new User("Naledi Khumalo", "Naledi@Example.com", "hash", Role.MECHANIC);
        ReflectionTestUtils.setField(user, "id", 42L);

        String token = jwtService.createToken(user);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("role", String.class)).isEqualTo("MECHANIC");
        assertThat(claims.get("email", String.class)).isEqualTo("naledi@example.com");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void shortSecretIsRefused() {
        assertThatThrownBy(() -> new JwtService("too-short", 60))
                .isInstanceOf(IllegalStateException.class);
    }
}
