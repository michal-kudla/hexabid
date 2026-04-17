package com.github.hexabid.core.auctioning.event;

import com.github.hexabid.core.auctioning.model.AuctionId;

import java.time.Instant;

public sealed interface AuctionDomainEvent permits AuctionLeaderChangedEvent, AuctionWonEvent, AuctionClosedWithoutWinnerEvent {
    AuctionId auctionId();

    Instant occurredAt();

    String type();
}
