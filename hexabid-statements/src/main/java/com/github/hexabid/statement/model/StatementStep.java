package com.github.hexabid.statement.model;

import java.util.Objects;

/**
 * An ordered step within a participation policy's statement workflow.
 *
 * <p>Steps group statements into a logical sequence for presentation
 * to the candidate, defining both the display order and a human-readable
 * label for the step.
 *
 * @param statementCode the statement this step represents
 * @param order         the 1-based display order within the workflow
 * @param label         a human-readable label for the step
 */
public record StatementStep(
        StatementCode statementCode,
        int order,
        String label
) {

    public StatementStep {
        Objects.requireNonNull(statementCode, "statementCode must not be null");
        Objects.requireNonNull(label, "label must not be null");
    }
}
