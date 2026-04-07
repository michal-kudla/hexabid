package com.acme.auctions.product;

import java.util.Objects;

/**
 * Human-readable name for a batch.
 * Example: "TH-2024-JASMINE-001"
 */
public record BatchName(String value) {

    public BatchName {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
