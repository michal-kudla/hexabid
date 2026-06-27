package com.github.hexabid.product;

import java.util.Objects;

public record TextualSerialNumber(String value) implements SerialNumber {

    public TextualSerialNumber {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
