package com.github.hexabid.core.auctioning.port.in;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.Price;

import java.util.Objects;

/**
 * Komenda edycji aukcji (tylko DRAFT).
 */
public record EditAuctionCommand(
        AuctionId auctionId,
        String title,
        Price startingPrice
) {
    public EditAuctionCommand {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(startingPrice, "startingPrice must not be null");
    }
}
