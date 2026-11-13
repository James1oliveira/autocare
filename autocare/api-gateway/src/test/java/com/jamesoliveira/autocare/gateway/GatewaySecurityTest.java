package com.jamesoliveira.autocare.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Checks that protected routes are blocked at the gateway, before any
 * request reaches a service (no services are running in this test).
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.jwt.secret=" + JwtValidatorTest.SECRET)
@AutoConfigureWebTestClient
class GatewaySecurityTest {

    @Autowired
    private WebTestClient client;

    @Test
    void bookingsNeedAToken() {
        client.get().uri("/api/bookings/mine")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody().jsonPath("$.error").isEqualTo("Please log in first");
    }

    @Test
    void invalidTokenIsRejected() {
        client.get().uri("/api/bookings/mine")
                .header("Authorization", "Bearer not-a-real-token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void fakeUserHeadersDoNotGetYouIn() {
        client.get().uri("/api/bookings")
                .header("X-User-Id", "1")
                .header("X-User-Role", "ADMIN")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void gatewayHealthIsPublic() {
        client.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }
}
