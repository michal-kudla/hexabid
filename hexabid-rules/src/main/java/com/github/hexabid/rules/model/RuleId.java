package com.github.hexabid.rules.model;

import java.util.UUID;

public record RuleId(UUID value) {

    public RuleId {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
    }

    public static RuleId newId() {
        return new RuleId(UUID.randomUUID());
    }

    public static RuleId of(UUID value) {
        return new RuleId(value);
    }
}
