package com.github.hexabid.core.auctioning.usecase;

import com.github.hexabid.core.auctioning.model.Auction;
import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.AuctionStatus;
import com.github.hexabid.core.auctioning.model.Price;
import com.github.hexabid.core.auctioning.port.in.EditAuctionCommand;
import com.github.hexabid.core.auctioning.port.in.EditAuctionResult;
import com.github.hexabid.core.auctioning.port.out.AuctionRepository;
import com.github.hexabid.core.lot.model.Lot;
import com.github.hexabid.core.party.model.PartyId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EditAuctionServiceTest {

    @Test
    void shouldEditDraftAuction() {
        var repository = new InMemoryAuctionRepository();
        var service = new EditAuctionService(repository);
        var auctionId = AuctionId.newId();
        var auction = Auction.create(auctionId, new PartyId("seller-1"), "dev:seller-1", "ORG",
                Lot.singleProductDraft("Old Title"), new Price(new BigDecimal("100.00"), "PLN"),
                Instant.parse("2026-06-01T12:00:00Z"));
        repository.save(auction);

        var result = service.editAuction(new EditAuctionCommand(auctionId, "New Title",
                new Price(new BigDecimal("200.00"), "PLN")));

        assertInstanceOf(EditAuctionResult.AuctionEdited.class, result);
        var edited = (EditAuctionResult.AuctionEdited) result;
        assertEquals("New Title", edited.auction().title());
    }

    @Test
    void shouldRejectEditWhenAuctionNotFound() {
        var repository = new InMemoryAuctionRepository();
        var service = new EditAuctionService(repository);

        var result = service.editAuction(new EditAuctionCommand(AuctionId.newId(), "Title",
                new Price(new BigDecimal("100.00"), "PLN")));

        assertInstanceOf(EditAuctionResult.AuctionNotFound.class, result);
    }

    @Test
    void shouldRejectEditWhenNotInDraft() {
        var repository = new InMemoryAuctionRepository();
        var service = new EditAuctionService(repository);
        var auctionId = AuctionId.newId();
        var auction = Auction.create(auctionId, new PartyId("seller-1"), "dev:seller-1", "ORG",
                Lot.singleProductDraft("Title"), new Price(new BigDecimal("100.00"), "PLN"),
                Instant.parse("2026-06-01T12:00:00Z"));
        auction.publish(Instant.now());
        repository.save(auction);

        var result = service.editAuction(new EditAuctionCommand(auctionId, "New Title",
                new Price(new BigDecimal("200.00"), "PLN")));

        assertInstanceOf(EditAuctionResult.EditNotAllowed.class, result);
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
            return List.of();
        }
    }
}
