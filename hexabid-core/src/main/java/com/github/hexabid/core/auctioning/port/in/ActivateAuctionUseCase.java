package com.github.hexabid.core.auctioning.port.in;

public interface ActivateAuctionUseCase {
    ActivateAuctionResult activateAuction(ActivateAuctionCommand command);
}
