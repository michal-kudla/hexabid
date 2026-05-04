package com.github.hexabid.core.auctioning.event;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.Price;

import java.time.Instant;
import java.util.Objects;

public record AuctionClosedBelowReserveEvent(
        AuctionId auctionId,
        Price currentPrice,
        Price reservePrice,
        Instant occurredAt
) implements AuctionDomainEvent {

    public AuctionClosedBelowReserveEvent {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(currentPrice, "currentPrice must not be null");
        Objects.requireNonNull(reservePrice, "reservePrice must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    @Override
    public String type() {
        return "AUCTION_CLOSED_BELOW_RESERVE";
    }
}
