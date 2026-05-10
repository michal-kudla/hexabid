package com.github.hexabid.statement.port.in;

import java.util.UUID;

/**
 * Command to start a new statement collection program.
 *
 * @param auctionId    the auction the candidate is applying to participate in
 * @param candidateId  the identifier of the candidate applying for participation
 * @param templateName the name of the policy template to instantiate (e.g. "PUBLIC_CONSUMER_LIGHT_V1")
 */
public record StartStatementProgramCommand(
        UUID auctionId,
        String candidateId,
        String templateName
) {}
