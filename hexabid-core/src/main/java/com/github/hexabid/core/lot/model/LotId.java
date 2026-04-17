package com.github.hexabid.core.lot.model;

import java.util.Objects;
import java.util.UUID;

public record LotId(UUID value) {

    public LotId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static LotId newId() {
        return new LotId(UUID.randomUUID());
    }

    public static LotId of(UUID value) {
        return new LotId(value);
    }
}
