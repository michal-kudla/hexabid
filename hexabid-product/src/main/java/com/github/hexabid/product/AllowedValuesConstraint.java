package com.github.hexabid.product;

import java.util.Set;

public record AllowedValuesConstraint(Set<Object> allowedValues) implements FeatureValueConstraint {

    public AllowedValuesConstraint {
        if (allowedValues == null || allowedValues.isEmpty()) {
            throw new IllegalArgumentException("allowedValues must not be null or empty");
        }
        allowedValues = Set.copyOf(allowedValues);
    }

    @Override
    public void validate(Object value) {
        if (!allowedValues.contains(value)) {
            throw new IllegalArgumentException(
                "Value '%s' is not allowed. Allowed values: %s".formatted(value, allowedValues));
        }
    }
}
