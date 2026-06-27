package com.github.hexabid.core.auctioning.port.out;

import com.github.hexabid.core.party.model.PartyId;

public interface KycClient {
    boolean isVerified(PartyId partyId);
}
