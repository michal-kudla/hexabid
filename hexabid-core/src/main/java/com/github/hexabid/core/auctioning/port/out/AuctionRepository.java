package com.github.hexabid.core.auctioning.port.out;

import com.github.hexabid.core.auctioning.model.Auction;
import com.github.hexabid.core.auctioning.model.AuctionId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuctionRepository {
    Auction save(Auction auction);

    Optional<Auction> findById(AuctionId auctionId);

    List<Auction> findExpiredOpenAuctions(Instant currentTime);
}
