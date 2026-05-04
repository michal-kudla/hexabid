package com.github.hexabid.core.auctioning.event;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.party.model.PartyId;

import java.time.Instant;
import java.util.Objects;

public record AuctionSettlementFailedEvent(
        AuctionId auctionId,
        PartyId winnerId,
        String reason,
        Instant occurredAt
) implements AuctionDomainEvent {

    public AuctionSettlementFailedEvent {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(winnerId, "winnerId must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    @Override
    public String type() {
        return "AUCTION_SETTLEMENT_FAILED";
    }
}
