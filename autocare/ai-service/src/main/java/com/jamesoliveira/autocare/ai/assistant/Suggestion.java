package com.jamesoliveira.autocare.ai.assistant;

/** A service the customer can book. Prices are null if the price list couldn't be loaded. */
public record Suggestion(String code, String name, Integer priceFrom, Integer priceTo) {
}
