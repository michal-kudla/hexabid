package com.github.hexabid.statement.model;

/**
 * Taxonomy of violation types that can arise from a candidate's statement answers.
 *
 * <p>Each type describes the nature of the violation, which determines
 * the severity of its consequence on the participation decision.
 */
public enum StatementViolationType {

    /** The candidate made a declaration that is automatically disqualifying. */
    FATAL_DECLARATION,

    /** The candidate's answers are internally contradictory. */
    CONTRADICTORY_DECLARATION,

    /** A required prerequisite statement has not been answered. */
    MISSING_PREREQUISITE,

    /** Supporting evidence or certification has expired. */
    EXPIRED_EVIDENCE,

    /** An external screening system returned an adverse hit. */
    EXTERNAL_ADVERSE_HIT,

    /** The answer was submitted after the deadline. */
    LATE_SUBMISSION,

    /** A material change occurred that was not declared by the candidate. */
    MATERIAL_CHANGE_NOT_DECLARED,

    /** The digital signature is invalid or missing. */
    SIGNATURE_INVALID
}
