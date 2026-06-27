package com.github.hexabid.product;

import java.util.List;
import java.util.Objects;

public record CompositeConstraint(Type type, List<ApplicabilityConstraint> constraints) implements ApplicabilityConstraint {

    public enum Type {
        AND, OR
    }

    public CompositeConstraint {
        Objects.requireNonNull(type, "type must not be null");
        if (constraints == null || constraints.isEmpty()) {
            throw new IllegalArgumentException("constraints must not be null or empty");
        }
        constraints = List.copyOf(constraints);
    }

    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return switch (type) {
            case AND -> constraints.stream().allMatch(c -> c.isSatisfiedBy(context));
            case OR -> constraints.stream().anyMatch(c -> c.isSatisfiedBy(context));
        };
    }
}
