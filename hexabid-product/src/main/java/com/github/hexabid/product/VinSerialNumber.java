package com.github.hexabid.product;

import java.util.Objects;

public record VinSerialNumber(String value) implements SerialNumber {

    public VinSerialNumber {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
