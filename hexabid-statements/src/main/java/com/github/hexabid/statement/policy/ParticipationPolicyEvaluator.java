package com.github.hexabid.statement.policy;

import com.github.hexabid.statement.graph.StatementDependencyGraph;
import com.github.hexabid.statement.model.StatementCode;
import com.github.hexabid.statement.model.StatementAnswer;
import com.github.hexabid.statement.model.StatementViolation;
import com.github.hexabid.statement.model.StatementViolationType;
import com.github.hexabid.statement.template.ParticipationPolicyTemplate;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Domain service that evaluates a set of submitted answers against a
 * {@link ParticipationPolicyTemplate} and produces an {@link EvaluationResult}.
 *
 * <p>The evaluator checks for disqualifying answers and missing statements.
 * A disqualifying answer triggers an immediate {@link EvaluationStatus#REJECTED}
 * result with the root cause and cascaded statements. If all statements are
 * answered positively, the result is {@link EvaluationStatus#ADMITTED}.
 * Otherwise, the result is {@link EvaluationStatus#PENDING}.
 *
 * @see EvaluationResult
 * @see ParticipationPolicyTemplate
 */
public final class ParticipationPolicyEvaluator {

    private final Clock clock;

    public ParticipationPolicyEvaluator(Clock clock) {
        this.clock = clock;
    }

    /**
     * Evaluates the given answers against the policy template.
     *
     * @param template the policy template defining required statements and their graph
     * @param answers  the map of submitted answers keyed by statement code
     * @return the evaluation result indicating admitted, rejected, or pending status
     */
    public EvaluationResult evaluate(
            ParticipationPolicyTemplate template,
            Map<StatementCode, StatementAnswer> answers
    ) {
        List<StatementViolation> violations = new ArrayList<>();
        StatementCode rootCause = null;
        List<StatementCode> cascadedStatements = new ArrayList<>();

        for (StatementAnswer answer : answers.values()) {
            if (answer.disqualifying()) {
                StatementViolation violation = StatementViolation.fatal(
                        answer.statementCode(),
                        StatementViolationType.FATAL_DECLARATION,
                        "Disqualifying answer submitted for " + answer.statementCode().value()
                );
                violations.add(violation);

                if (rootCause == null) {
                    rootCause = answer.statementCode();
                    Set<StatementCode> reachable = template.graph().reachableFrom(answer.statementCode());
                    cascadedStatements = new ArrayList<>(reachable);
                }
            }
        }

        if (rootCause != null) {
            return EvaluationResult.rejected(violations, rootCause, cascadedStatements);
        }

        Set<StatementCode> completedStatements = answers.keySet();
        List<StatementCode> missingStatements = new ArrayList<>();
        for (StatementCode code : template.graph().nodes()) {
            if (!completedStatements.contains(code)) {
                missingStatements.add(code);
            }
        }

        if (missingStatements.isEmpty()) {
            return EvaluationResult.admitted(violations);
        }

        return EvaluationResult.pending(violations, missingStatements);
    }

    /**
     * The status of a policy evaluation.
     */
    public enum EvaluationStatus { ADMITTED, REJECTED, PENDING }

    /**
     * Result of evaluating a set of answers against a policy template.
     *
     * @param violations         detected violations, if any
     * @param status             the evaluation outcome
     * @param rootCause          the statement code that caused rejection, or {@code null}
     * @param cascadedStatements statements cancelled by the root cause rejection
     * @param missingStatements  statements that have not yet been answered
     */
    public record EvaluationResult(
            List<StatementViolation> violations,
            EvaluationStatus status,
            StatementCode rootCause,
            List<StatementCode> cascadedStatements,
            List<StatementCode> missingStatements
    ) {
        public EvaluationResult {
            violations = List.copyOf(violations);
            cascadedStatements = cascadedStatements != null ? List.copyOf(cascadedStatements) : List.of();
            missingStatements = missingStatements != null ? List.copyOf(missingStatements) : List.of();
        }

        /**
         * Creates a rejected result with the given violations, root cause, and cascaded statements.
         */
        public static EvaluationResult rejected(List<StatementViolation> violations, StatementCode rootCause, List<StatementCode> cascadedStatements) {
            return new EvaluationResult(violations, EvaluationStatus.REJECTED, rootCause, cascadedStatements, List.of());
        }

        /**
         * Creates an admitted result with no missing statements.
         */
        public static EvaluationResult admitted(List<StatementViolation> violations) {
            return new EvaluationResult(violations, EvaluationStatus.ADMITTED, null, List.of(), List.of());
        }

        /**
         * Creates a pending result listing missing statements.
         */
        public static EvaluationResult pending(List<StatementViolation> violations, List<StatementCode> missingStatements) {
            return new EvaluationResult(violations, EvaluationStatus.PENDING, null, List.of(), missingStatements);
        }
    }
}
