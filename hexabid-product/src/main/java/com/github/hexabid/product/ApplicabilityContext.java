package com.github.hexabid.product;

import java.util.Map;
import java.util.Objects;

/**
 * Context for evaluating applicability constraints.
 * Contains key-value pairs representing the context in which a product is being considered.
 */
public record ApplicabilityContext(java.util.Map<String, Object> attributes) {

    public ApplicabilityContext {
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null");
        }
        attributes = Map.copyOf(attributes);
    }

    public static ApplicabilityContext empty() {
        return new ApplicabilityContext(Map.of());
    }

    public static ApplicabilityContext of(String key, Object value) {
        return new ApplicabilityContext(Map.of(key, value));
    }

    public Object get(String key) {
        return attributes.get(key);
    }

    public boolean has(String key) {
        return attributes.containsKey(key);
    }
}
