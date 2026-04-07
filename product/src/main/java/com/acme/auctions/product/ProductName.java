package com.acme.auctions.product;

import java.util.Objects;

public record ProductName(String value) {
    public ProductName {
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
