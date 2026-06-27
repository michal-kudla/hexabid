package com.github.hexabid.statement.model;

import java.time.Instant;
import java.util.Objects;

/**
 * A candidate's answer to a single statement within a program instance.
 *
 * <p>Answers are submitted via {@link StatementProgramInstance#submitAnswer}
 * and are immutable once created. If the answer matches a disqualifying value
 * defined in the {@link StatementDefinition}, the {@code disqualifying} flag
 * is set to {@code true}.
 *
 * @param id                 the unique answer identifier
 * @param programInstanceId  the program instance this answer belongs to
 * @param statementCode      the statement this answer responds to
 * @param answerValue        the raw answer value provided by the candidate
 * @param disqualifying      whether this answer triggers disqualification
 * @param submittedAt        the instant the answer was submitted
 */
public record StatementAnswer(
        StatementAnswerId id,
        StatementProgramInstanceId programInstanceId,
        StatementCode statementCode,
        String answerValue,
        boolean disqualifying,
        Instant submittedAt
) {

    public StatementAnswer {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(programInstanceId, "programInstanceId must not be null");
        Objects.requireNonNull(statementCode, "statementCode must not be null");
        Objects.requireNonNull(answerValue, "answerValue must not be null");
        Objects.requireNonNull(submittedAt, "submittedAt must not be null");
    }

    /**
     * Creates a new answer with a generated identifier.
     *
     * @param programInstanceId the program instance this answer belongs to
     * @param statementCode     the statement being answered
     * @param answerValue       the candidate's answer value
     * @param disqualifying     whether this answer is disqualifying
     * @param submittedAt       the submission instant
     * @return a new statement answer
     */
    public static StatementAnswer create(
            StatementProgramInstanceId programInstanceId,
            StatementCode statementCode,
            String answerValue,
            boolean disqualifying,
            Instant submittedAt
    ) {
        return new StatementAnswer(
                StatementAnswerId.newId(),
                programInstanceId,
                statementCode,
                answerValue,
                disqualifying,
                submittedAt
        );
    }
}
