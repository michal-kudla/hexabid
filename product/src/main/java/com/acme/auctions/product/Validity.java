package com.acme.auctions.product;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a validity period for catalog entries.
 */
public record Validity(LocalDate from, LocalDate to) {

    public Validity {
        Objects.requireNonNull(from, "from must not be null");
        if (to != null && to.isBefore(from)) {
            throw new IllegalArgumentException("to must be >= from");
        }
    }

    public static Validity from(LocalDate from) {
        return new Validity(from, null);
    }

    public static Validity between(LocalDate from, LocalDate to) {
        return new Validity(from, to);
    }

    public Validity to(LocalDate to) {
        return new Validity(from, to);
    }

    public boolean isValidAt(LocalDate date) {
        if (!date.isBefore(from)) {
            return to == null || !date.isAfter(to);
        }
        return false;
    }
}
