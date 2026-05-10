package com.github.hexabid.statement.port.in;

/**
 * Inbound port for retrieving the current state of a statement collection program.
 *
 * <p>Returns a read-only view of the program including all statement steps,
 * their statuses, and the current participation decision.
 *
 * @see GetStatementProgramQuery
 * @see StatementProgramView
 */
public interface GetStatementProgramUseCase {

    /**
     * Retrieves the statement program for the given query.
     *
     * @param query the query identifying the program by auction and candidate
     * @return the view of the requested program
     */
    StatementProgramView getProgram(GetStatementProgramQuery query);
}
