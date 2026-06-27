package com.github.hexabid.statement.port.in;

/**
 * Inbound port for submitting an answer to a statement within a program.
 *
 * <p>Processes the candidate's answer and returns a result indicating
 * whether the answer was accepted, rejected, or prerequisites were not met.
 *
 * @see SubmitStatementAnswerCommand
 * @see SubmitStatementAnswerResult
 */
public interface SubmitStatementAnswerUseCase {

    /**
     * Submits an answer to a statement in the candidate's program.
     *
     * @param command the command containing auction, candidate, statement, and answer details
     * @return the result of the submission attempt
     */
    SubmitStatementAnswerResult submitAnswer(SubmitStatementAnswerCommand command);
}
