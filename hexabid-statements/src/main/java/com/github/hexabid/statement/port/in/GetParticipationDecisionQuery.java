package com.github.hexabid.statement.port.in;

import java.util.UUID;

/**
 * Query to retrieve the participation decision for a candidate in an auction.
 *
 * @param auctionId   the auction the candidate is applying to participate in
 * @param candidateId the identifier of the candidate whose decision is being queried
 */
public record GetParticipationDecisionQuery(
        UUID auctionId,
        String candidateId
) {}
