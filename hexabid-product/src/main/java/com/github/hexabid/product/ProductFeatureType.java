package com.github.hexabid.product;

import java.util.Objects;

/**
 * Defines a named feature type with an optional constraint on its values.
 * Example: "color" with AllowedValuesConstraint(red, green, blue),
 *          "yearOfProduction" with NumericRangeConstraint(1990, 2024).
 */
public record ProductFeatureType(String name, FeatureValueConstraint constraint) {

    public ProductFeatureType {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(constraint, "constraint must not be null");
    }

    public static ProductFeatureType unconstrained(String name) {
        return new ProductFeatureType(name, Unconstrained.INSTANCE);
    }

    public void validate(Object value) {
        constraint.validate(value);
    }
}
