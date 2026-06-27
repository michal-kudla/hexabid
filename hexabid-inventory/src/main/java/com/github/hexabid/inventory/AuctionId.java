package com.github.hexabid.inventory;

import java.util.Objects;
import java.util.UUID;

public record AuctionId(UUID value) {

    public AuctionId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static AuctionId next() {
        return new AuctionId(UUID.randomUUID());
    }

    public static AuctionId of(String value) {
        return new AuctionId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
