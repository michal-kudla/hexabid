package com.github.hexabid.statement.model;

import java.util.Objects;

/**
 * An answer value that, if selected by a candidate, automatically disqualifies
 * them from participation.
 *
 * <p>Each disqualifying answer pairs the raw answer value with a human-readable
 * label explaining why the answer is disqualifying.
 *
 * @param answerValue the raw answer value that triggers disqualification
 * @param humanLabel  a human-readable explanation of the disqualification reason
 */
public record DisqualifyingAnswer(
        String answerValue,
        String humanLabel
) {

    public DisqualifyingAnswer {
        Objects.requireNonNull(answerValue, "answerValue must not be null");
        Objects.requireNonNull(humanLabel, "humanLabel must not be null");
    }

    /**
     * Factory method for creating a disqualifying answer.
     *
     * @param value the raw answer value
     * @param label the human-readable disqualification reason
     * @return a new disqualifying answer
     */
    public static DisqualifyingAnswer of(String value, String label) {
        return new DisqualifyingAnswer(value, label);
    }
}
