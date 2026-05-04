package com.github.hexabid.core.auctioning.port.out;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.DocumentRequirement;
import com.github.hexabid.core.auctioning.model.DocumentStatus;
import com.github.hexabid.core.auctioning.model.DocumentType;
import com.github.hexabid.core.party.model.PartyId;

import java.util.List;

public interface DocumentRepository {
    DocumentStatus getDocumentStatus(PartyId partyId, DocumentType type);

    void submitDocument(PartyId partyId, DocumentType type, DocumentStatus status);

    List<DocumentRequirement> getRequirementsForAuction(AuctionId auctionId);
}
