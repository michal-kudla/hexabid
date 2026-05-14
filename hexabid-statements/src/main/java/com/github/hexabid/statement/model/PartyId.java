package com.github.hexabid.statement.model;

import java.util.Objects;

/**
 * Unique identifier for a party (candidate or organiser) within the Hexabid domain.
 *
 * <p>Parties are identified by an externally-assigned string key rather than a UUID,
 * allowing integration with identity providers that use opaque identifiers.
 *
 * @param value the party identifier, must not be null or blank
 */
public record PartyId(String value) {

    public PartyId {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    /**
     * Creates a party identifier from a string value.
     *
     * @param value the party identifier string
     * @return a party identifier wrapping the given value
     */
    public static PartyId of(String value) {
        return new PartyId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
