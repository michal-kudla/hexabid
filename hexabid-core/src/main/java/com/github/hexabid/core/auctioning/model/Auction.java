package com.github.hexabid.core.auctioning.model;

import com.github.hexabid.core.auctioning.event.AuctionClosedBelowReserveEvent;
import com.github.hexabid.core.auctioning.event.AuctionClosedWithoutWinnerEvent;
import com.github.hexabid.core.auctioning.event.AuctionDomainEvent;
import com.github.hexabid.core.auctioning.event.AuctionLeaderChangedEvent;
import com.github.hexabid.core.auctioning.event.AuctionPublishedEvent;
import com.github.hexabid.core.auctioning.event.AuctionReofferedEvent;
import com.github.hexabid.core.auctioning.event.AuctionSettledEvent;
import com.github.hexabid.core.auctioning.event.AuctionSettlementFailedEvent;
import com.github.hexabid.core.auctioning.event.AuctionStartedEvent;
import com.github.hexabid.core.auctioning.event.AuctionWonEvent;
import com.github.hexabid.core.auctioning.exception.AuctionClosedForBiddingException;
import com.github.hexabid.core.auctioning.exception.AuctionExpiredForBiddingException;
import com.github.hexabid.core.auctioning.exception.BidAmountTooLowException;
import com.github.hexabid.core.auctioning.exception.SellerCannotBidOnOwnAuctionException;
import com.github.hexabid.core.lot.model.Lot;
import com.github.hexabid.core.party.model.PartyId;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Auction {

    private final AuctionId id;
    private final PartyId sellerId;
    private final Lot lot;
    private final Price startingPrice;
    private final Instant endsAt;
    private final @Nullable Long version;
    private AuctionStatus status;
    private final List<Bid> biddingHistory;
    private @Nullable String participationPolicyTemplate;

    private Auction(
            AuctionId id,
            PartyId sellerId,
            Lot lot,
            Price startingPrice,
            Instant endsAt,
            @Nullable Long version,
            AuctionStatus status,
            List<Bid> biddingHistory,
            @Nullable String participationPolicyTemplate
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId must not be null");
        this.lot = Objects.requireNonNull(lot, "lot must not be null");
        this.startingPrice = Objects.requireNonNull(startingPrice, "startingPrice must not be null");
        this.endsAt = Objects.requireNonNull(endsAt, "endsAt must not be null");
        this.version = version;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.biddingHistory = new ArrayList<>(Objects.requireNonNull(biddingHistory, "biddingHistory must not be null"));
        this.participationPolicyTemplate = participationPolicyTemplate;
    }

    public static Auction create(AuctionId id, PartyId sellerId, Lot lot, Price startingPrice, Instant endsAt) {
        return new Auction(id, sellerId, lot, startingPrice, endsAt, null, AuctionStatus.DRAFT, List.of(), null);
    }

    public static Auction rehydrate(
            AuctionId id,
            PartyId sellerId,
            Lot lot,
            Price startingPrice,
            Instant endsAt,
            @Nullable Long version,
            AuctionStatus status,
            List<Bid> biddingHistory,
            @Nullable String participationPolicyTemplate
    ) {
        return new Auction(id, sellerId, lot, startingPrice, endsAt, version, status, biddingHistory, participationPolicyTemplate);
    }

    public AuctionDomainEvent publish(Instant publishedAt) {
        ensureStatusIs(AuctionStatus.DRAFT);
        Objects.requireNonNull(publishedAt, "publishedAt must not be null");
        status = AuctionStatus.PUBLISHED;
        return new AuctionPublishedEvent(id, publishedAt);
    }

    public AuctionDomainEvent start(Instant startedAt) {
        ensureStatusIs(AuctionStatus.PUBLISHED);
        Objects.requireNonNull(startedAt, "startedAt must not be null");
        status = AuctionStatus.IN_PROGRESS;
        return new AuctionStartedEvent(id, startedAt);
    }

    public PlaceBidDecision placeBid(PartyId bidderId, Price amount, Instant placedAt) {
        ensureInProgressAt(placedAt);
        Objects.requireNonNull(bidderId, "bidderId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(placedAt, "placedAt must not be null");
        if (sellerId.equals(bidderId)) {
            throw new SellerCannotBidOnOwnAuctionException();
        }
        if (!amount.isGreaterThan(currentPrice())) {
            throw new BidAmountTooLowException();
        }

        Bid previousLeader = maybeLeadingBid().orElse(null);
        Bid bid = new Bid(bidderId, amount, placedAt);
        biddingHistory.add(bid);

        PartyId previousLeaderId = previousLeader == null ? null : previousLeader.bidderId();
        Optional<AuctionLeaderChangedEvent> leaderChangedEvent = Optional.of(new AuctionLeaderChangedEvent(
                id,
                previousLeaderId,
                bidderId,
                amount,
                placedAt
        ));

        return new PlaceBidDecision(bid, leaderChangedEvent);
    }

    public AuctionDomainEvent closeWithWinner(Instant closedAt) {
        ensureStatusIs(AuctionStatus.IN_PROGRESS);
        Objects.requireNonNull(closedAt, "closedAt must not be null");
        var leadingBid = maybeLeadingBid().orElseThrow(() -> new IllegalStateException("cannot close auction without bids"));

        if (leadingBid.amount().compareTo(lot.reservePrice()) < 0) {
            status = AuctionStatus.CLOSED;
            return new AuctionClosedBelowReserveEvent(id, leadingBid.amount(), lot.reservePrice(), closedAt);
        }

        status = AuctionStatus.PENDING_SETTLEMENT;
        return new AuctionWonEvent(id, leadingBid.bidderId(), leadingBid.amount(), closedAt);
    }

    public Optional<AuctionDomainEvent> closeIfExpired(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        if (status != AuctionStatus.IN_PROGRESS || now.isBefore(endsAt)) {
            return Optional.empty();
        }

        if (biddingHistory.isEmpty()) {
            status = AuctionStatus.CLOSED;
            return Optional.of(new AuctionClosedWithoutWinnerEvent(id, now));
        }

        var leadingBid = maybeLeadingBid().orElseThrow();
        if (leadingBid.amount().compareTo(lot.reservePrice()) < 0) {
            status = AuctionStatus.CLOSED;
            return Optional.of(new AuctionClosedBelowReserveEvent(id, leadingBid.amount(), lot.reservePrice(), now));
        }

        status = AuctionStatus.PENDING_SETTLEMENT;
        return Optional.of(new AuctionWonEvent(id, leadingBid.bidderId(), leadingBid.amount(), now));
    }

    public AuctionDomainEvent markAsSettled(Instant settledAt) {
        ensureStatusIs(AuctionStatus.PENDING_SETTLEMENT);
        Objects.requireNonNull(settledAt, "settledAt must not be null");
        var winnerId = maybeWinnerId().orElseThrow(() -> new IllegalStateException("cannot settle auction without winner"));
        status = AuctionStatus.SETTLED;
        return new AuctionSettledEvent(id, winnerId, settledAt);
    }

    public AuctionDomainEvent markSettlementFailed(String reason, Instant failedAt) {
        ensureStatusIs(AuctionStatus.PENDING_SETTLEMENT);
        Objects.requireNonNull(reason, "reason must not be null");
        Objects.requireNonNull(failedAt, "failedAt must not be null");
        var winnerId = maybeWinnerId().orElseThrow(() -> new IllegalStateException("cannot fail settlement without winner"));
        status = AuctionStatus.FAILED_SETTLEMENT;
        return new AuctionSettlementFailedEvent(id, winnerId, reason, failedAt);
    }

    public AuctionDomainEvent reoffer(Instant reofferedAt) {
        ensureStatusIs(AuctionStatus.FAILED_SETTLEMENT);
        Objects.requireNonNull(reofferedAt, "reofferedAt must not be null");
        status = AuctionStatus.REOFFERED;
        return new AuctionReofferedEvent(id, reofferedAt);
    }

    @Deprecated(forRemoval = true)
    public Optional<AuctionDomainEvent> maybeCloseIfExpired(Instant now) {
        return closeIfExpired(now);
    }

    public AuctionId id() {
        return id;
    }

    public PartyId sellerId() {
        return sellerId;
    }

    public String title() {
        return lot.title();
    }

    public Lot lot() {
        return lot;
    }

    public Price startingPrice() {
        return startingPrice;
    }

    public Instant endsAt() {
        return endsAt;
    }

    public Optional<Long> maybeVersion() {
        return Optional.ofNullable(version);
    }

    public AuctionStatus status() {
        return status;
    }

    public List<Bid> biddingHistory() {
        return List.copyOf(biddingHistory);
    }

    public Price currentPrice() {
        return maybeLeadingBid().map(Bid::amount).orElse(startingPrice);
    }

    public Optional<PartyId> maybeWinnerId() {
        return maybeLeadingBid().map(Bid::bidderId);
    }

    public boolean isExpiredAt(Instant now) {
        return !now.isBefore(endsAt);
    }

    public boolean isBiddable() {
        return status == AuctionStatus.IN_PROGRESS;
    }

    public @Nullable String participationPolicyTemplate() {
        return participationPolicyTemplate;
    }

    public void assignParticipationPolicyTemplate(@Nullable String templateName) {
        this.participationPolicyTemplate = templateName;
    }

    private Optional<Bid> maybeLeadingBid() {
        if (biddingHistory.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(biddingHistory.getLast());
    }

    private void ensureStatusIs(AuctionStatus expected) {
        if (status != expected) {
            throw new AuctionClosedForBiddingException();
        }
    }

    private void ensureInProgressAt(Instant instant) {
        if (status == AuctionStatus.CLOSED || status == AuctionStatus.SETTLED
                || status == AuctionStatus.FAILED_SETTLEMENT || status == AuctionStatus.REOFFERED) {
            throw new AuctionClosedForBiddingException();
        }
        if (status != AuctionStatus.IN_PROGRESS) {
            throw new AuctionClosedForBiddingException();
        }
        if (!instant.isBefore(endsAt)) {
            throw new AuctionExpiredForBiddingException();
        }
    }

    public record PlaceBidDecision(Bid acceptedBid, Optional<AuctionLeaderChangedEvent> leaderChangedEvent) {
    }
}
