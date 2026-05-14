package com.github.hexabid.statement.port.in;

import org.jspecify.annotations.Nullable;

/**
 * Read-only view of a single statement step within a program, returned to the adapter layer.
 *
 * @param statementCode the unique code identifying this statement
 * @param title         the human-readable title of the statement
 * @param question      the question posed to the candidate
 * @param answerType    the expected answer type (e.g. YES_NO, TEXT)
 * @param order         the display order of this step within the program
 * @param stepLabel     the label of the step group this statement belongs to
 * @param answerValue   the candidate's answer, or {@code null} if not yet answered
 */
public record StatementStepView(
        String statementCode,
        String title,
        String question,
        String answerType,
        int order,
        String stepLabel,
        @Nullable String answerValue
) {}
