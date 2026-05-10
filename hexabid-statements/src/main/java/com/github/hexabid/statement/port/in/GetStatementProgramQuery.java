package com.github.hexabid.statement.port.in;

import java.util.UUID;

/**
 * Query to retrieve the current state of a statement collection program.
 *
 * @param auctionId   the auction the candidate is applying to participate in
 * @param candidateId the identifier of the candidate whose program is being queried
 */
public record GetStatementProgramQuery(
        UUID auctionId,
        String candidateId
) {}
