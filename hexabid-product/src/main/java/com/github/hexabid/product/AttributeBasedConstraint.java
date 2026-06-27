package com.github.hexabid.product;

import java.util.Objects;
import java.util.function.Predicate;

public record AttributeBasedConstraint(String key, Predicate<Object> predicate) implements ApplicabilityConstraint {

    public AttributeBasedConstraint {
        Objects.requireNonNull(key, "key must not be null");
        if (key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        Objects.requireNonNull(predicate, "predicate must not be null");
    }

    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        if (!context.has(key)) {
            return false;
        }
        return predicate.test(context.get(key));
    }
}
