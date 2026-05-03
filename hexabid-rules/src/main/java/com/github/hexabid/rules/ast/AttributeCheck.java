package com.github.hexabid.rules.ast;

import java.util.Objects;

public record AttributeCheck(AttributeKey key, Comparator comparator, Object value) implements Expression {

    public AttributeCheck {
        Objects.requireNonNull(key);
        Objects.requireNonNull(comparator);
        if (comparator != Comparator.IS_NULL && comparator != Comparator.IS_NOT_NULL) {
            Objects.requireNonNull(value);
        }
    }

    public enum Comparator {
        EQUALS,
        NOT_EQUALS,
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL,
        IN,
        NOT_IN,
        IS_NULL,
        IS_NOT_NULL
    }
}
