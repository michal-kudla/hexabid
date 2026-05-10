package com.github.hexabid.statement.model;

import java.util.List;
import java.util.Objects;

/**
 * Declarative definition of a single statement that a candidate must answer
 * during participation qualification.
 *
 * <p>A definition specifies the code, category, question, expected answer type,
 * severity, disqualifying answers, and possible violations. Definitions are
 * composed into {@link ParticipationPolicyTemplate} instances and should not
 * contain auction-specific conditions — those belong in the template.
 *
 * @see ParticipationPolicyTemplate
 * @see StatementCode
 */
public record StatementDefinition(
        StatementCode code,
        StatementCategory category,
        String title,
        String question,
        AnswerType answerType,
        StatementSeverity severity,
        List<DisqualifyingAnswer> disqualifyingAnswers,
        List<StatementViolationDefinition> possibleViolations
) {

    public StatementDefinition {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(question, "question must not be null");
        Objects.requireNonNull(answerType, "answerType must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        disqualifyingAnswers = List.copyOf(Objects.requireNonNull(disqualifyingAnswers));
        possibleViolations = List.copyOf(Objects.requireNonNull(possibleViolations));
    }

    /**
     * Checks whether the given answer value matches any of the disqualifying
     * answers defined for this statement.
     *
     * @param answerValue the candidate's answer value
     * @return {@code true} if this answer disqualifies the candidate
     */
    public boolean isDisqualifyingAnswer(String answerValue) {
        return disqualifyingAnswers.stream()
                .anyMatch(da -> da.answerValue().equals(answerValue));
    }
}
