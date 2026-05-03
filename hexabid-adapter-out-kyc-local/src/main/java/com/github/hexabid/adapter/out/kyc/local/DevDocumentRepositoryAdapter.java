package com.github.hexabid.adapter.out.kyc.local;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.DocumentRequirement;
import com.github.hexabid.core.auctioning.model.DocumentStatus;
import com.github.hexabid.core.auctioning.model.DocumentType;
import com.github.hexabid.core.auctioning.port.out.DocumentRepository;
import com.github.hexabid.core.party.model.PartyId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Primary
@Component
public class DevDocumentRepositoryAdapter implements DocumentRepository {

    private final Map<String, DocumentStatus> storage = new ConcurrentHashMap<>();

    @Override
    public DocumentStatus getDocumentStatus(PartyId partyId, DocumentType type) {
        return storage.getOrDefault(key(partyId, type), DocumentStatus.MISSING);
    }

    @Override
    public void submitDocument(PartyId partyId, DocumentType type, DocumentStatus status) {
        storage.put(key(partyId, type), status);
    }

    @Override
    public List<DocumentRequirement> getRequirementsForAuction(AuctionId auctionId) {
        return List.of();
    }

    private static String key(PartyId partyId, DocumentType type) {
        return partyId.value() + ":" + type.name();
    }
}
