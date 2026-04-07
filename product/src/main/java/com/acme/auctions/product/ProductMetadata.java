package com.acme.auctions.product;

import java.util.Map;

public record ProductMetadata(Map<String, String> values) {

    public ProductMetadata {
        if (values == null) {
            throw new IllegalArgumentException("values must not be null");
        }
        values = Map.copyOf(values);
    }

    public static ProductMetadata empty() {
        return new ProductMetadata(Map.of());
    }

    public static ProductMetadata of(Map<String, String> values) {
        return new ProductMetadata(values);
    }

    public String get(String key) {
        return values.get(key);
    }

    public String getOrDefault(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }
}
