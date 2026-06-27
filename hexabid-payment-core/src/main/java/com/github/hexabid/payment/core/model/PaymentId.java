package com.github.hexabid.payment.core.model;

import java.util.Objects;
import java.util.UUID;

public record PaymentId(UUID value) {

    public PaymentId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static PaymentId next() {
        return new PaymentId(UUID.randomUUID());
    }

    public static PaymentId of(String value) {
        return new PaymentId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
