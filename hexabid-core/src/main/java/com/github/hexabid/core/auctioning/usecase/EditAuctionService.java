package com.github.hexabid.core.auctioning.usecase;

import com.github.hexabid.core.auctioning.model.Auction;
import com.github.hexabid.core.auctioning.port.in.EditAuctionCommand;
import com.github.hexabid.core.auctioning.port.in.EditAuctionResult;
import com.github.hexabid.core.auctioning.port.in.EditAuctionUseCase;
import com.github.hexabid.core.auctioning.port.out.AuctionRepository;
import java.util.Objects;

public final class EditAuctionService implements EditAuctionUseCase {

    private final AuctionRepository auctionRepository;

    public EditAuctionService(AuctionRepository auctionRepository) {
        this.auctionRepository = Objects.requireNonNull(auctionRepository, "auctionRepository must not be null");
    }

    @Override
    public EditAuctionResult editAuction(EditAuctionCommand command) {
        var existing = auctionRepository.findById(command.auctionId());
        if (existing.isEmpty()) {
            return new EditAuctionResult.AuctionNotFound(command.auctionId());
        }

        Auction auction = existing.get();
        if (auction.status() != com.github.hexabid.core.auctioning.model.AuctionStatus.DRAFT) {
            return new EditAuctionResult.EditNotAllowed(
                    "Only DRAFT auctions can be edited. Current status: " + auction.status());
        }

        Auction edited = auction.edit(command.title(), command.startingPrice());
        var saved = auctionRepository.save(edited);
        return new EditAuctionResult.AuctionEdited(AuctionViews.from(saved));
    }
}
