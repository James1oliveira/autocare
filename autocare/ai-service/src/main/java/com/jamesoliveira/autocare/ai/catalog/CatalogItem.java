package com.jamesoliveira.autocare.ai.catalog;

public record CatalogItem(String code, String name, String description, int priceFrom, int priceTo, String estimatedTime) {
}
