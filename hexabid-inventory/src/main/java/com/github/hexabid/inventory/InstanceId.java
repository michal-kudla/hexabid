package com.github.hexabid.inventory;

import java.util.Objects;
import java.util.UUID;

public record InstanceId(UUID value) {

    public InstanceId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static InstanceId random() {
        return new InstanceId(UUID.randomUUID());
    }

    public static InstanceId of(UUID value) {
        return new InstanceId(value);
    }
}
