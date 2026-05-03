package com.github.hexabid.core.auctioning.usecase;

import com.github.hexabid.core.auctioning.event.AuctionDomainEvent;
import com.github.hexabid.core.auctioning.model.Auction;
import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.AuctionStatus;
import com.github.hexabid.core.auctioning.model.Price;
import com.github.hexabid.core.auctioning.port.in.SettleAuctionCommand;
import com.github.hexabid.core.auctioning.port.in.SettlementResult;
import com.github.hexabid.core.auctioning.port.out.AuctionEventPublisher;
import com.github.hexabid.core.auctioning.port.out.AuctionRepository;
import com.github.hexabid.core.lot.model.Lot;
import com.github.hexabid.core.party.model.PartyId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SettleAuctionServiceTest {

    private static Auction createAuctionPendingSettlement(AuctionId id, PartyId sellerId, String title,
                                                           Price startingPrice, Instant endsAt) {
        var beforeEnd = endsAt.minusSeconds(600);
        var auction = Auction.create(id, sellerId, Lot.singleProductDraft(title), startingPrice, endsAt);
        auction.publish(beforeEnd);
        auction.start(beforeEnd);
        auction.placeBid(new PartyId("bidder-1"), new Price(new BigDecimal("150.00"), "PLN"), beforeEnd.plusSeconds(60));
        auction.closeWithWinner(beforeEnd.plusSeconds(120));
        return auction;
    }

    @Test
    void shouldSettleAuctionInPendingSettlement() {
        var now = Instant.parse("2026-03-05T12:00:00Z");
        var endsAt = now.plusSeconds(3600);
        var clock = Clock.fixed(now, ZoneOffset.UTC);
        var repository = new InMemoryAuctionRepository();
        var events = new RecordingAuctionEventPublisher();

        var auction = createAuctionPendingSettlement(
                AuctionId.newId(),
                new PartyId("seller-1"),
                "Painting",
                new Price(new BigDecimal("100.00"), "PLN"),
                endsAt
        );
        repository.save(auction);

        var service = new SettleAuctionService(repository, events, clock);
        var result = service.settleAuction(new SettleAuctionCommand(auction.id()));

        assertInstanceOf(SettlementResult.SettlementSucceeded.class, result);
        assertEquals(AuctionStatus.SETTLED, repository.findById(auction.id()).orElseThrow().status());
        assertEquals(1, events.events.size());
    }

    @Test
    void shouldFailSettlementWhenAuctionNotFound() {
        var now = Instant.parse("2026-03-05T12:00:00Z");
        var clock = Clock.fixed(now, ZoneOffset.UTC);
        var repository = new InMemoryAuctionRepository();
        var events = new RecordingAuctionEventPublisher();

        var service = new SettleAuctionService(repository, events, clock);
        var result = service.settleAuction(new SettleAuctionCommand(AuctionId.newId()));

        assertInstanceOf(SettlementResult.SettlementFailed.class, result);
    }

    @Test
    void shouldFailSettlementWhenNotInPendingSettlement() {
        var now = Instant.parse("2026-03-05T12:00:00Z");
        var clock = Clock.fixed(now, ZoneOffset.UTC);
        var repository = new InMemoryAuctionRepository();
        var events = new RecordingAuctionEventPublisher();

        var auction = Auction.create(
                AuctionId.newId(),
                new PartyId("seller-1"),
                Lot.singleProductDraft("Draft item"),
                new Price(new BigDecimal("100.00"), "PLN"),
                now.plusSeconds(3600)
        );
        repository.save(auction);

        var service = new SettleAuctionService(repository, events, clock);
        var result = service.settleAuction(new SettleAuctionCommand(auction.id()));

        var failed = assertInstanceOf(SettlementResult.SettlementFailed.class, result);
        assertEquals(AuctionStatus.DRAFT, auction.status());
    }

    private static final class InMemoryAuctionRepository implements AuctionRepository {

        private final Map<AuctionId, Auction> storage = new HashMap<>();

        @Override
        public Auction save(Auction auction) {
            storage.put(auction.id(), auction);
            return auction;
        }

        @Override
        public Optional<Auction> findById(AuctionId auctionId) {
            return Optional.ofNullable(storage.get(auctionId));
        }

        @Override
        public List<Auction> findExpiredOpenAuctions(Instant currentTime) {
            return List.of();
        }

        @Override
        public List<Auction> findExpiredInProgressAuctions(Instant currentTime) {
            return List.of();
        }

        @Override
        public List<Auction> findPendingSettlementAuctions() {
            return storage.values().stream()
                    .filter(a -> a.status() == AuctionStatus.PENDING_SETTLEMENT)
                    .toList();
        }
    }

    private static final class RecordingAuctionEventPublisher implements AuctionEventPublisher {

        private final List<AuctionDomainEvent> events = new ArrayList<>();

        @Override
        public void publish(AuctionDomainEvent event) {
            events.add(event);
        }
    }
}
