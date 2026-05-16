package com.github.hexabid.core.auctioning.port.in;

import com.github.hexabid.core.auctioning.model.AuctionId;

/**
 * Wynik edycji aukcji.
 */
public sealed interface EditAuctionResult {

    record AuctionEdited(AuctionView auction) implements EditAuctionResult {}

    record AuctionNotFound(AuctionId auctionId) implements EditAuctionResult {}

    record EditNotAllowed(String reason) implements EditAuctionResult {}
}
