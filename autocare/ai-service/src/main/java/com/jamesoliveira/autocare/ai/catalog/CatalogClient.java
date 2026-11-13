package com.jamesoliveira.autocare.ai.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Gets the live price list from booking-service (service-to-service call).
 * Results are cached for 5 minutes, and if booking-service is down the
 * assistant still works, just without prices.
 */
@Component
public class CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogClient.class);
    private static final Duration CACHE_TIME = Duration.ofMinutes(5);

    private final RestClient restClient;
    private volatile Map<String, CatalogItem> cache = Map.of();
    private volatile Instant fetchedAt = Instant.EPOCH;

    public CatalogClient(@Value("${app.booking-service-url}") String bookingServiceUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(2).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());
        this.restClient = RestClient.builder()
                .baseUrl(bookingServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public Map<String, CatalogItem> catalog() {
        boolean fresh = !cache.isEmpty() && Instant.now().isBefore(fetchedAt.plus(CACHE_TIME));
        if (fresh) {
            return cache;
        }
        try {
            List<CatalogItem> items = restClient.get()
                    .uri("/api/catalog")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CatalogItem>>() {
                    });
            if (items != null && !items.isEmpty()) {
                cache = items.stream().collect(Collectors.toMap(CatalogItem::code, Function.identity(), (a, b) -> a));
                fetchedAt = Instant.now();
            }
        } catch (Exception e) {
            log.warn("Could not load the price list from booking-service: {}", e.getMessage());
        }
        return cache;
    }
}
