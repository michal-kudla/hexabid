package com.github.hexabid.core.auctioning.event;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.party.model.PartyId;

import java.time.Instant;
import java.util.Objects;

public record AuctionSettledEvent(
        AuctionId auctionId,
        PartyId winnerId,
        Instant occurredAt
) implements AuctionDomainEvent {

    public AuctionSettledEvent {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(winnerId, "winnerId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    @Override
    public String type() {
        return "AUCTION_SETTLED";
    }
}
