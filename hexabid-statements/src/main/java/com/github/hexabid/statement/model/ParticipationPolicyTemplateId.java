package com.github.hexabid.statement.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Unique identifier for a {@link ParticipationPolicyTemplate}.
 *
 * @param value the underlying UUID
 */
public record ParticipationPolicyTemplateId(UUID value) {

    public ParticipationPolicyTemplateId {
        Objects.requireNonNull(value, "value must not be null");
    }

    /**
     * Generates a new random policy template identifier.
     *
     * @return a new identifier with a random UUID
     */
    public static ParticipationPolicyTemplateId newId() {
        return new ParticipationPolicyTemplateId(UUID.randomUUID());
    }

    /**
     * Creates an identifier from an existing UUID value.
     *
     * @param value the UUID value
     * @return a policy template identifier wrapping the given UUID
     */
    public static ParticipationPolicyTemplateId of(UUID value) {
        return new ParticipationPolicyTemplateId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
