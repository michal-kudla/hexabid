package com.github.hexabid.core.auctioning.exception;

public abstract class AuctionBusinessException extends RuntimeException {

    protected AuctionBusinessException(String message) {
        super(message);
    }
}
