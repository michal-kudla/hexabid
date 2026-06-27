package com.github.hexabid.core.auctioning.exception;

public final class AuctionExpiredForBiddingException extends AuctionBusinessException {

    public AuctionExpiredForBiddingException() {
        super("auction already expired");
    }
}
