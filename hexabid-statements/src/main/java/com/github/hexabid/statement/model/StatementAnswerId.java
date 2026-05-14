package com.github.hexabid.statement.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Unique identifier for a {@link StatementAnswer}.
 *
 * @param value the underlying UUID
 */
public record StatementAnswerId(UUID value) {

    public StatementAnswerId {
        Objects.requireNonNull(value, "value must not be null");
    }

    /**
     * Generates a new random answer identifier.
     *
     * @return a new identifier with a random UUID
     */
    public static StatementAnswerId newId() {
        return new StatementAnswerId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
