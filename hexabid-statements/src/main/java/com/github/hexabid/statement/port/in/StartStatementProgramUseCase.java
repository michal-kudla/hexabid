package com.github.hexabid.statement.port.in;

/**
 * Inbound port for starting a new statement collection program.
 *
 * <p>Initiates the participation qualification process for a candidate
 * in a specific auction, based on a named policy template.
 *
 * @see StartStatementProgramCommand
 * @see StatementProgramView
 */
public interface StartStatementProgramUseCase {

    /**
     * Starts a new statement program for the given command.
     *
     * @param command the command containing auction, candidate, and template information
     * @return the view of the newly created program instance
     */
    StatementProgramView startProgram(StartStatementProgramCommand command);
}
