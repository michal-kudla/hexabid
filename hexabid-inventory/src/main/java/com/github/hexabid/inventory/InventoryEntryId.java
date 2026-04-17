package com.github.hexabid.inventory;

import com.github.hexabid.product.ProductIdentifier;

import java.util.Objects;
import java.util.UUID;

public record InventoryEntryId(UUID value) {

    public InventoryEntryId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static InventoryEntryId random() {
        return new InventoryEntryId(UUID.randomUUID());
    }

    public static InventoryEntryId of(UUID value) {
        return new InventoryEntryId(value);
    }
}
