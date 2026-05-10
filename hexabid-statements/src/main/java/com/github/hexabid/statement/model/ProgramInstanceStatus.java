package com.github.hexabid.statement.model;

/**
 * Lifecycle status of a {@link StatementProgramInstance}.
 *
 * <p>Instances start {@link #IN_PROGRESS}, and transition to a terminal
 * state once all statements are answered or the candidate is rejected.
 */
public enum ProgramInstanceStatus {

    /** The candidate is actively answering statements. */
    IN_PROGRESS,

    /** All statements answered; the participation decision has been made. */
    COMPLETED,

    /** The candidate has been disqualified from participation. */
    REJECTED,

    /** The candidate or organiser has withdrawn the program instance. */
    CANCELLED
}
