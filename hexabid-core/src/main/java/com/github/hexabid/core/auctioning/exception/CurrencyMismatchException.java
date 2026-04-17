package com.github.hexabid.core.auctioning.exception;

public final class CurrencyMismatchException extends AuctionBusinessException {

    public CurrencyMismatchException() {
        super("currency mismatch");
    }
}
