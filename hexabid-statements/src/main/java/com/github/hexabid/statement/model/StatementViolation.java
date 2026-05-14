package com.github.hexabid.statement.model;

import java.util.Objects;

/**
 * A concrete violation recorded against a candidate's statement answer.
 *
 * <p>Unlike {@link StatementViolationDefinition} which is declarative, this
 * record represents an actual violation that has occurred during participation
 * qualification. Violations are either fatal (causing automatic disqualification)
 * or non-fatal (requiring review).
 *
 * @param type           the violation type
 * @param statementCode  the statement the violation relates to
 * @param description    a human-readable description of the violation
 * @param fatal          whether this violation is fatal and causes automatic disqualification
 */
public record StatementViolation(
        StatementViolationType type,
        StatementCode statementCode,
        String description,
        boolean fatal
) {

    public StatementViolation {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(statementCode, "statementCode must not be null");
        Objects.requireNonNull(description, "description must not be null");
    }

    /**
     * Creates a fatal violation that causes automatic disqualification.
     *
     * @param code        the statement code
     * @param type        the violation type
     * @param description a human-readable description
     * @return a fatal violation
     */
    public static StatementViolation fatal(StatementCode code, StatementViolationType type, String description) {
        return new StatementViolation(type, code, description, true);
    }

    /**
     * Creates a non-fatal violation that requires review but does not auto-disqualify.
     *
     * @param code        the statement code
     * @param type        the violation type
     * @param description a human-readable description
     * @return a non-fatal violation
     */
    public static StatementViolation nonFatal(StatementCode code, StatementViolationType type, String description) {
        return new StatementViolation(type, code, description, false);
    }
}
