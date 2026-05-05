package com.github.hexabid.core.auctioning.port.in;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.party.model.PartyId;

import java.util.Objects;

public record ActivateAuctionCommand(AuctionId auctionId, PartyId actorId) {

    public ActivateAuctionCommand {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(actorId, "actorId must not be null");
    }
}
