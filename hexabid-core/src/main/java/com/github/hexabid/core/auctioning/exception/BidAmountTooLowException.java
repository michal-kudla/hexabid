package com.github.hexabid.core.auctioning.exception;

public final class BidAmountTooLowException extends AuctionBusinessException {

    public BidAmountTooLowException() {
        super("bid must be greater than current price");
    }
}
