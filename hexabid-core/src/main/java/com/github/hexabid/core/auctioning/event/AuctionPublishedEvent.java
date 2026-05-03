package com.github.hexabid.core.auctioning.event;

import com.github.hexabid.core.auctioning.model.AuctionId;

import java.time.Instant;
import java.util.Objects;

public record AuctionPublishedEvent(
        AuctionId auctionId,
        Instant occurredAt
) implements AuctionDomainEvent {

    public AuctionPublishedEvent {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    @Override
    public String type() {
        return "AUCTION_PUBLISHED";
    }
}
