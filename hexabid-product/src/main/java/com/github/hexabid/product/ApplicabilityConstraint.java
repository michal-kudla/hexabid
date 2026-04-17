package com.github.hexabid.product;

import java.util.List;
import java.util.function.Predicate;

/**
 * Constraint that determines whether a product is applicable in a given context.
 */
public sealed interface ApplicabilityConstraint
    permits AlwaysTrueConstraint, AttributeBasedConstraint, CompositeConstraint {

    boolean isSatisfiedBy(ApplicabilityContext context);

    static ApplicabilityConstraint alwaysTrue() {
        return AlwaysTrueConstraint.INSTANCE;
    }

    static ApplicabilityConstraint requiresAttribute(String key, Predicate<Object> predicate) {
        return new AttributeBasedConstraint(key, predicate);
    }

    static ApplicabilityConstraint allOf(ApplicabilityConstraint... constraints) {
        return new CompositeConstraint(CompositeConstraint.Type.AND, List.of(constraints));
    }

    static ApplicabilityConstraint anyOf(ApplicabilityConstraint... constraints) {
        return new CompositeConstraint(CompositeConstraint.Type.OR, List.of(constraints));
    }
}
