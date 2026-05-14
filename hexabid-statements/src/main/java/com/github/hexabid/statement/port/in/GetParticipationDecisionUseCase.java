package com.github.hexabid.statement.port.in;

/**
 * Inbound port for retrieving the participation decision for a candidate in an auction.
 *
 * <p>Returns a read-only view of the decision indicating whether the candidate
 * is admitted, conditionally admitted, pending, or rejected.
 *
 * @see GetParticipationDecisionQuery
 * @see ParticipationDecisionView
 */
public interface GetParticipationDecisionUseCase {

    /**
     * Retrieves the participation decision for the given query.
     *
     * @param query the query identifying the decision by auction and candidate
     * @return the view of the participation decision
     */
    ParticipationDecisionView getDecision(GetParticipationDecisionQuery query);
}
