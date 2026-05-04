package com.github.hexabid.core.auctioning.port.in;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.DocumentStatus;
import com.github.hexabid.core.auctioning.model.DocumentType;
import com.github.hexabid.core.party.model.PartyId;

import java.util.Objects;

public record SubmitDocumentCommand(
        PartyId partyId,
        AuctionId auctionId,
        DocumentType documentType,
        DocumentStatus status
) {

    public SubmitDocumentCommand {
        Objects.requireNonNull(partyId, "partyId must not be null");
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(documentType, "documentType must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }
}
