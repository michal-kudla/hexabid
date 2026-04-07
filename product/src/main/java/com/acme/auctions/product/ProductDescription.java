package com.acme.auctions.product;

import java.util.Objects;

public record ProductDescription(String value) {
    public ProductDescription {
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
