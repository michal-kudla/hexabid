package com.github.hexabid.statement.model;

import java.util.Objects;

/**
 * Monotonically increasing version number of a {@link ParticipationPolicyTemplate}.
 *
 * <p>Versions start at 1. When a template's statements or dependencies change
 * in a way that affects qualification semantics, a new version must be created.
 *
 * @param value the version number, must be &ge; 1
 */
public record PolicyTemplateVersion(int value) {

    public PolicyTemplateVersion {
        if (value < 1) {
            throw new IllegalArgumentException("version must be >= 1");
        }
    }

    /**
     * Creates a version from an integer value.
     *
     * @param value the version number, must be &ge; 1
     * @return a policy template version
     */
    public static PolicyTemplateVersion of(int value) {
        return new PolicyTemplateVersion(value);
    }

    /**
     * Returns the first version of any template.
     *
     * @return version 1
     */
    public static PolicyTemplateVersion v1() {
        return new PolicyTemplateVersion(1);
    }
}
