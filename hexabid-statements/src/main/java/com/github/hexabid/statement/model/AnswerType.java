package com.github.hexabid.statement.model;

/**
 * The type of answer a candidate must provide for a statement.
 *
 * <p>Determines the input control and validation rules for the answer value.
 */
public enum AnswerType {

    /** A simple yes/no boolean declaration. */
    YES_NO,

    /** Free-form text response. */
    TEXT,

    /** Selection of exactly one option from a predefined list. */
    SINGLE_CHOICE,

    /** Selection of zero or more options from a predefined list. */
    MULTI_CHOICE,

    /** A numeric value. */
    NUMERIC,

    /** Upload of a supporting document as evidence. */
    DOCUMENT_UPLOAD
}
