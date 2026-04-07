package com.acme.auctions.product;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicabilityConstraintTest {

    @Test
    void shouldAlwaysBeSatisfiedWhenAlwaysTrue() {
        ApplicabilityConstraint constraint = ApplicabilityConstraint.alwaysTrue();

        assertTrue(constraint.isSatisfiedBy(ApplicabilityContext.empty()));
        assertTrue(constraint.isSatisfiedBy(ApplicabilityContext.of("age", 25)));
    }

    @Test
    void shouldSatisfyAttributeConstraintWhenPresentAndMatches() {
        ApplicabilityConstraint constraint = ApplicabilityConstraint.requiresAttribute(
            "age", v -> v instanceof Integer && (Integer) v >= 18);

        assertTrue(constraint.isSatisfiedBy(ApplicabilityContext.of("age", 25)));
    }

    @Test
    void shouldFailAttributeConstraintWhenMissing() {
        ApplicabilityConstraint constraint = ApplicabilityConstraint.requiresAttribute(
            "age", v -> v instanceof Integer && (Integer) v >= 18);

        assertFalse(constraint.isSatisfiedBy(ApplicabilityContext.empty()));
    }

    @Test
    void shouldFailAttributeConstraintWhenValueDoesNotMatch() {
        ApplicabilityConstraint constraint = ApplicabilityConstraint.requiresAttribute(
            "age", v -> v instanceof Integer && (Integer) v >= 18);

        assertFalse(constraint.isSatisfiedBy(ApplicabilityContext.of("age", 15)));
    }

    @Test
    void shouldSatisfyAllOfWhenAllConstraintsSatisfied() {
        ApplicabilityConstraint constraint = ApplicabilityConstraint.allOf(
            ApplicabilityConstraint.requiresAttribute("age", v -> (Integer) v >= 18),
            ApplicabilityConstraint.requiresAttribute("region", v -> "PL".equals(v))
        );

        ApplicabilityContext context = new ApplicabilityContext(java.util.Map.of("age", 25, "region", "PL"));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldFailAllOfWhenOneConstraintFails() {
        ApplicabilityConstraint constraint = ApplicabilityConstraint.allOf(
            ApplicabilityConstraint.requiresAttribute("age", v -> (Integer) v >= 18),
            ApplicabilityConstraint.requiresAttribute("region", v -> "PL".equals(v))
        );

        ApplicabilityContext context = new ApplicabilityContext(java.util.Map.of("age", 25, "region", "DE"));

        assertFalse(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldSatisfyAnyOfWhenAtLeastOneConstraintSatisfied() {
        ApplicabilityConstraint constraint = ApplicabilityConstraint.anyOf(
            ApplicabilityConstraint.requiresAttribute("premium", v -> Boolean.TRUE.equals(v)),
            ApplicabilityConstraint.requiresAttribute("vip", v -> Boolean.TRUE.equals(v))
        );

        ApplicabilityContext context = new ApplicabilityContext(java.util.Map.of("premium", false, "vip", true));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldFailAnyOfWhenNoConstraintSatisfied() {
        ApplicabilityConstraint constraint = ApplicabilityConstraint.anyOf(
            ApplicabilityConstraint.requiresAttribute("premium", v -> Boolean.TRUE.equals(v)),
            ApplicabilityConstraint.requiresAttribute("vip", v -> Boolean.TRUE.equals(v))
        );

        ApplicabilityContext context = new ApplicabilityContext(java.util.Map.of("premium", false, "vip", false));

        assertFalse(constraint.isSatisfiedBy(context));
    }
}
