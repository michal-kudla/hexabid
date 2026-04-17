package com.github.hexabid.product;

/**
 * Constraint that validates feature values.
 */
public sealed interface FeatureValueConstraint
    permits AllowedValuesConstraint, NumericRangeConstraint,
            DecimalRangeConstraint, DateRangeConstraint, RegexConstraint, Unconstrained {

    void validate(Object value);
}
