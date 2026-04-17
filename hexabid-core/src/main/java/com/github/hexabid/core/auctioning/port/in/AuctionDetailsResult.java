package com.github.hexabid.core.auctioning.port.in;

import com.github.hexabid.core.auctioning.model.AuctionId;

import java.util.Objects;

public sealed interface AuctionDetailsResult permits AuctionDetailsResult.AuctionFound, AuctionDetailsResult.AuctionMissing {

    record AuctionFound(AuctionView auction) implements AuctionDetailsResult {

        public AuctionFound {
            Objects.requireNonNull(auction, "auction must not be null");
        }
    }

    record AuctionMissing(AuctionId auctionId) implements AuctionDetailsResult {

        public AuctionMissing {
            Objects.requireNonNull(auctionId, "auctionId must not be null");
        }
    }
}
