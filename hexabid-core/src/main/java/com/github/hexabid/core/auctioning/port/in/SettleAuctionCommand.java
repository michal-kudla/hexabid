package com.github.hexabid.core.auctioning.port.in;

import com.github.hexabid.core.auctioning.model.AuctionId;

import java.util.Objects;

public record SettleAuctionCommand(AuctionId auctionId) {

    public SettleAuctionCommand {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
    }
}
