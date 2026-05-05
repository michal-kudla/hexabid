package com.github.hexabid.core.auctioning.usecase;

import com.github.hexabid.core.auctioning.event.AuctionDomainEvent;
import com.github.hexabid.core.auctioning.model.AuctionStatus;
import com.github.hexabid.core.auctioning.port.in.ActivateAuctionCommand;
import com.github.hexabid.core.auctioning.port.in.ActivateAuctionFailureReason;
import com.github.hexabid.core.auctioning.port.in.ActivateAuctionResult;
import com.github.hexabid.core.auctioning.port.in.ActivateAuctionUseCase;
import com.github.hexabid.core.auctioning.port.out.AuctionEventPublisher;
import com.github.hexabid.core.auctioning.port.out.AuctionRepository;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Objects;

public final class ActivateAuctionService implements ActivateAuctionUseCase {

    private final AuctionRepository auctionRepository;
    private final AuctionEventPublisher eventPublisher;
    private final Clock clock;

    public ActivateAuctionService(AuctionRepository auctionRepository, AuctionEventPublisher eventPublisher, Clock clock) {
        this.auctionRepository = Objects.requireNonNull(auctionRepository, "auctionRepository must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public ActivateAuctionResult activateAuction(ActivateAuctionCommand command) {
        var auction = auctionRepository.findById(command.auctionId()).orElse(null);
        if (auction == null) {
            return rejected(ActivateAuctionFailureReason.AUCTION_NOT_FOUND, "auction does not exist");
        }
        if (!auction.sellerId().equals(command.actorId())) {
            return rejected(ActivateAuctionFailureReason.ACTOR_IS_NOT_SELLER, "only the seller can activate this auction");
        }
        if (auction.endsAt().isBefore(clock.instant())) {
            return rejected(ActivateAuctionFailureReason.AUCTION_NOT_READY, "auction end time must still be in the future");
        }

        var events = new ArrayList<AuctionDomainEvent>();
        try {
            if (auction.status() == AuctionStatus.DRAFT) {
                events.add(auction.publish(clock.instant()));
            }
            if (auction.status() == AuctionStatus.PUBLISHED) {
                events.add(auction.start(clock.instant()));
            }
        } catch (IllegalStateException exception) {
            return rejected(ActivateAuctionFailureReason.AUCTION_NOT_READY, exception.getMessage());
        }

        var saved = auctionRepository.save(auction);
        events.forEach(eventPublisher::publish);
        return new ActivateAuctionResult.AuctionActivated(AuctionViews.from(saved));
    }

    private static ActivateAuctionResult.AuctionActivationRejected rejected(
            ActivateAuctionFailureReason reason,
            String message
    ) {
        return new ActivateAuctionResult.AuctionActivationRejected(reason, message);
    }
}
