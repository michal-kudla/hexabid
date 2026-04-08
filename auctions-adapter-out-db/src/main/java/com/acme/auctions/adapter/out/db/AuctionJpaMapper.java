package com.acme.auctions.adapter.out.db;

import com.acme.auctions.core.auctioning.model.Auction;
import com.acme.auctions.core.auctioning.model.AuctionId;
import com.acme.auctions.core.auctioning.model.Bid;
import com.acme.auctions.core.auctioning.model.Price;
import com.acme.auctions.core.lot.model.Lot;
import com.acme.auctions.core.party.model.PartyId;
import org.springframework.stereotype.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class AuctionJpaMapper {

    Auction toDomain(AuctionJpaEntity entity) {
        return Auction.rehydrate(
                new AuctionId(entity.getId()),
                new PartyId(entity.getSellerId()),
                Lot.singleProductDraft(entity.getTitle()),
                new Price(entity.getStartingPrice(), entity.getCurrency()),
                entity.getEndsAt(),
                entity.getVersion(),
                entity.getStatus(),
                entity.getBids().stream()
                        .map(this::toDomain)
                        .toList()
        );
    }

    AuctionJpaEntity toEntity(Auction auction, AuctionJpaEntity entity) {
        entity.setId(auction.id().value());
        entity.setSellerId(auction.sellerId().value());
        entity.setTitle(auction.lot().title());
        entity.setProductName(auction.lot().title());
        entity.setStartingPrice(auction.startingPrice().amount());
        entity.setCurrentPrice(auction.currentPrice().amount());
        entity.setCurrency(auction.currentPrice().currency());
        entity.setLeadingBidderId(auction.maybeWinnerId().map(PartyId::value).orElse(null));
        entity.setEndsAt(auction.endsAt());
        entity.setStatus(auction.status());
        entity.setVersion(auction.maybeVersion().orElse(null));

        entity.getBids().clear();
        var bids = new ArrayList<BidJpaEntity>();
        for (Bid bid : auction.biddingHistory()) {
            BidJpaEntity bidEntity = new BidJpaEntity();
            bidEntity.setAuction(entity);
            bidEntity.setBidderId(bid.bidderId().value());
            bidEntity.setAmount(bid.amount().amount());
            bidEntity.setCurrency(bid.amount().currency());
            bidEntity.setPlacedAt(bid.placedAt());
            bids.add(bidEntity);
        }
        entity.getBids().addAll(bids);
        return entity;
    }

    private Bid toDomain(BidJpaEntity entity) {
        return new Bid(
                new PartyId(entity.getBidderId()),
                new Price(entity.getAmount(), entity.getCurrency()),
                entity.getPlacedAt()
        );
    }
}
