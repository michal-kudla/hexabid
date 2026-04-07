package com.acme.auctions.product;

import java.util.Objects;
import java.util.UUID;

/**
 * Unique identifier for a product batch (production run).
 */
public record BatchId(UUID value) {

    public BatchId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static BatchId random() {
        return new BatchId(UUID.randomUUID());
    }

    public static BatchId of(UUID value) {
        return new BatchId(value);
    }
}
