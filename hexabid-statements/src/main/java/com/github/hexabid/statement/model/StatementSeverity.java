package com.github.hexabid.statement.model;

/**
 * Impact severity of a statement within a participation policy.
 *
 * <p>Severity drives how violations of the statement affect the candidate's
 * participation outcome — from automatic disqualification to informational notices.
 */
public enum StatementSeverity {

    /** Violation results in automatic disqualification. */
    BLOCKING,

    /** Violation requires review but does not auto-disqualify. */
    IMPORTANT,

    /** For informational purposes only; no direct impact on qualification. */
    INFORMATIONAL
}
