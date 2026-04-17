package com.github.hexabid.core.auctioning.exception;

import com.github.hexabid.core.auctioning.model.AuctionId;

public final class AuctionConcurrencyConflictException extends AuctionBusinessException {

    public AuctionConcurrencyConflictException(AuctionId auctionId) {
        super("auction state changed concurrently: " + auctionId);
    }
}
