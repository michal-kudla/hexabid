package com.github.hexabid.statement.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Unique identifier for an auction within the Hexabid domain.
 *
 * <p>Used as a foreign key reference in the statements module to link
 * a program instance to the auction it qualifies the candidate for.
 *
 * @param value the underlying UUID
 */
public record AuctionId(UUID value) {

    public AuctionId {
        Objects.requireNonNull(value, "value must not be null");
    }

    /**
     * Creates an auction identifier from an existing UUID value.
     *
     * @param value the UUID value
     * @return an auction identifier wrapping the given UUID
     */
    public static AuctionId of(UUID value) {
        return new AuctionId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
