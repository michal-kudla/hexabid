package com.github.hexabid.product;

import java.time.Instant;

public record DateRangeConstraint(Instant start, Instant end) implements FeatureValueConstraint {

    public DateRangeConstraint {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end must not be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start must be <= end");
        }
    }

    @Override
    public void validate(Object value) {
        if (!(value instanceof Instant)) {
            throw new IllegalArgumentException("Value must be an Instant");
        }
        Instant instantValue = (Instant) value;
        if (instantValue.isBefore(start) || instantValue.isAfter(end)) {
            throw new IllegalArgumentException(
                "Value %s is out of range [%s, %s]".formatted(value, start, end));
        }
    }
}
