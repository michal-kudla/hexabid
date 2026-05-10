package com.github.hexabid.statement.model;

/**
 * Domain classification of participation statements.
 *
 * <p>Categories group statements by the aspect of candidate eligibility
 * they verify, enabling filtering and reporting by concern area.
 */
public enum StatementCategory {

    /** Statements verifying legal identity, capacity, and representation. */
    IDENTITY,

    /** Statements ensuring regulatory and sanctions compliance. */
    COMPLIANCE,

    /** Statements related to financial capacity and funding sources. */
    FINANCING,

    /** Statements concerning tax residency and obligations. */
    TAX,

    /** Statements covering sector-specific regulatory requirements. */
    REGULATORY,

    /** Statements verifying professional or domain-specific qualifications. */
    SUBJECT_KNOWLEDGE,

    /** Statements enforcing confidentiality of sensitive auction data. */
    CONFIDENTIALITY,

    /** Statements ensuring fair competition and absence of collusion. */
    FAIRNESS,

    /** Statements confirming the candidate's ability to execute obligations. */
    EXECUTION,

    /** Statements recording binding commitments and acceptance of terms. */
    COMMITMENT
}
