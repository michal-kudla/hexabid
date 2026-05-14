package com.github.hexabid.statement.model;

/**
 * Lifecycle status of a {@link StatementProgramInstance}.
 *
 * <p>Instances start as {@link #NOT_STARTED} when a program is assigned but not yet begun,
 * transition to {@link #IN_PROGRESS} when the candidate starts answering, and reach a terminal
 * state once all statements are answered or the candidate is rejected.
 */
public enum ProgramInstanceStatus {

    /** The program has been assigned but the candidate has not yet started. */
    NOT_STARTED,

    /** The candidate is actively answering statements. */
    IN_PROGRESS,

    /** All statements answered; the participation decision has been made. */
    COMPLETED,

    /** The candidate has been disqualified from participation. */
    REJECTED,

    /** The candidate or organiser has withdrawn the program instance. */
    CANCELLED
}
