package com.github.hexabid.core.auctioning.port.in;

public interface CreateAuctionUseCase {
    CreateAuctionResult createAuction(CreateAuctionCommand command);
}
