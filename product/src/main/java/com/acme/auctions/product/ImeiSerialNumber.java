package com.acme.auctions.product;

import java.util.Objects;

public record ImeiSerialNumber(String value) implements SerialNumber {

    public ImeiSerialNumber {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
