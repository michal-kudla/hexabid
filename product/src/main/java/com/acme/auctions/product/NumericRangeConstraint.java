package com.acme.auctions.product;

import java.math.BigDecimal;

public record NumericRangeConstraint(BigDecimal min, BigDecimal max) implements FeatureValueConstraint {

    public NumericRangeConstraint {
        if (min == null || max == null) {
            throw new IllegalArgumentException("min and max must not be null");
        }
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("min must be <= max");
        }
    }

    @Override
    public void validate(Object value) {
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Value must be a number");
        }
        BigDecimal numericValue = new BigDecimal(value.toString());
        if (numericValue.compareTo(min) < 0 || numericValue.compareTo(max) > 0) {
            throw new IllegalArgumentException(
                "Value %s is out of range [%s, %s]".formatted(value, min, max));
        }
    }
}
