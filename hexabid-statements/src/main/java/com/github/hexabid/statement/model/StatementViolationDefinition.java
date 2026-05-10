package com.github.hexabid.statement.model;

import java.util.Objects;

/**
 * Declarative definition of a violation that can arise from a specific statement.
 *
 * <p>Violation definitions are part of a {@link StatementDefinition} and describe
 * the possible violations that may be recorded when a candidate's answer
 * does not satisfy the statement's requirements.
 *
 * @param type           the category of violation
 * @param statementCode  the statement this violation relates to
 * @param description    a human-readable description of the violation
 */
public record StatementViolationDefinition(
        StatementViolationType type,
        StatementCode statementCode,
        String description
) {

    public StatementViolationDefinition {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(statementCode, "statementCode must not be null");
        Objects.requireNonNull(description, "description must not be null");
    }
}
