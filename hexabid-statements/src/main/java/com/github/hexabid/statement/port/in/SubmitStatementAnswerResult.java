package com.github.hexabid.statement.port.in;

import java.util.List;

/**
 * Sealed hierarchy representing the outcome of a statement answer submission.
 *
 * <p>A submission can result in:
 * <ul>
 *   <li>{@link AnswerAccepted} — the answer was recorded and the program continues</li>
 *   <li>{@link AnswerRejected} — the answer triggered a disqualifying violation</li>
 *   <li>{@link PrerequisiteNotMet} — prerequisites for this statement have not been satisfied</li>
 * </ul>
 *
 * @see SubmitStatementAnswerUseCase
 */
public sealed interface SubmitStatementAnswerResult {

    /**
     * The answer was accepted and recorded in the program.
     *
     * @param programView the updated view of the program after accepting the answer
     */
    record AnswerAccepted(StatementProgramView programView) implements SubmitStatementAnswerResult {}

    /**
     * The answer was rejected because it triggered a disqualifying violation.
     *
     * @param programView the updated view of the program after the rejection
     * @param reason      a human-readable explanation of why the answer was rejected
     */
    record AnswerRejected(StatementProgramView programView, String reason) implements SubmitStatementAnswerResult {}

    /**
     * The answer could not be submitted because prerequisite statements have not been answered.
     *
     * @param statementCode        the code of the statement whose prerequisites were not met
     * @param missingPrerequisites the list of statement codes that must be answered first
     */
    record PrerequisiteNotMet(String statementCode, List<String> missingPrerequisites) implements SubmitStatementAnswerResult {}
}
