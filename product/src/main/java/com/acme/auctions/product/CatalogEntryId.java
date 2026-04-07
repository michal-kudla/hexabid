package com.acme.auctions.product;

import java.util.Objects;
import java.util.UUID;

public record CatalogEntryId(UUID value) {

    public CatalogEntryId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static CatalogEntryId random() {
        return new CatalogEntryId(UUID.randomUUID());
    }

    public static CatalogEntryId of(UUID value) {
        return new CatalogEntryId(value);
    }
}
