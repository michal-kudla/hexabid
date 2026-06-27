package com.github.hexabid.core.auctioning.port.in;

import java.util.Objects;

public sealed interface ActivateAuctionResult permits ActivateAuctionResult.AuctionActivated, ActivateAuctionResult.AuctionActivationRejected {

    record AuctionActivated(AuctionView auction) implements ActivateAuctionResult {

        public AuctionActivated {
            Objects.requireNonNull(auction, "auction must not be null");
        }
    }

    record AuctionActivationRejected(ActivateAuctionFailureReason reason, String message) implements ActivateAuctionResult {

        public AuctionActivationRejected {
            Objects.requireNonNull(reason, "reason must not be null");
            Objects.requireNonNull(message, "message must not be null");
        }
    }
}
