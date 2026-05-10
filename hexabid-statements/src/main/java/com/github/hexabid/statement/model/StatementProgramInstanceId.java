package com.github.hexabid.statement.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Unique identifier for a {@link StatementProgramInstance}.
 *
 * @param value the underlying UUID
 */
public record StatementProgramInstanceId(UUID value) {

    public StatementProgramInstanceId {
        Objects.requireNonNull(value, "value must not be null");
    }

    /**
     * Generates a new random program instance identifier.
     *
     * @return a new identifier with a random UUID
     */
    public static StatementProgramInstanceId newId() {
        return new StatementProgramInstanceId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
