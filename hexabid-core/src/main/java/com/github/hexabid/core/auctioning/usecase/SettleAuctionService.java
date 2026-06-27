package com.github.hexabid.core.auctioning.usecase;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.AuctionStatus;
import com.github.hexabid.core.auctioning.port.in.SettleAuctionCommand;
import com.github.hexabid.core.auctioning.port.in.SettlementResult;
import com.github.hexabid.core.auctioning.port.in.SettleAuctionUseCase;
import com.github.hexabid.core.auctioning.port.out.AuctionEventPublisher;
import com.github.hexabid.core.auctioning.port.out.AuctionRepository;

import java.time.Clock;
import java.util.Objects;

public final class SettleAuctionService implements SettleAuctionUseCase {

    private final AuctionRepository auctionRepository;
    private final AuctionEventPublisher eventPublisher;
    private final Clock clock;

    public SettleAuctionService(AuctionRepository auctionRepository, AuctionEventPublisher eventPublisher, Clock clock) {
        this.auctionRepository = Objects.requireNonNull(auctionRepository, "auctionRepository must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public SettlementResult settleAuction(SettleAuctionCommand command) {
        var auction = auctionRepository.findById(command.auctionId()).orElse(null);
        if (auction == null) {
            return new SettlementResult.SettlementFailed("auction not found");
        }
        if (auction.status() != AuctionStatus.PENDING_SETTLEMENT) {
            return new SettlementResult.SettlementFailed("auction is not in PENDING_SETTLEMENT state, current: " + auction.status());
        }

        var event = auction.markAsSettled(clock.instant());
        auctionRepository.save(auction);
        eventPublisher.publish(event);
        return new SettlementResult.SettlementSucceeded();
    }
}
