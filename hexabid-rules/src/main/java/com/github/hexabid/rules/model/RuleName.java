package com.github.hexabid.rules.model;

public record RuleName(String value) {

    public RuleName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    public static RuleName of(String value) {
        return new RuleName(value);
    }
}
