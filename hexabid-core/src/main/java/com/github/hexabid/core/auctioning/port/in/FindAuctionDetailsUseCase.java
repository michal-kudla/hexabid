package com.github.hexabid.core.auctioning.port.in;

import com.github.hexabid.core.auctioning.model.AuctionId;

public interface FindAuctionDetailsUseCase {
    AuctionDetailsResult findAuctionDetails(AuctionId auctionId);
}
