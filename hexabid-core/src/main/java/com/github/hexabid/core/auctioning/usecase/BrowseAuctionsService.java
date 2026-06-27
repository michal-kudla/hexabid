package com.github.hexabid.core.auctioning.usecase;

import com.github.hexabid.core.auctioning.port.in.AuctionBrowsePage;
import com.github.hexabid.core.auctioning.port.in.BrowseAuctionsQuery;
import com.github.hexabid.core.auctioning.port.in.BrowseAuctionsUseCase;
import com.github.hexabid.core.auctioning.port.out.AuctionReadModel;
import com.github.hexabid.core.party.model.PartyId;

import java.util.Objects;

public final class BrowseAuctionsService implements BrowseAuctionsUseCase {

    private final AuctionReadModel auctionReadModel;

    public BrowseAuctionsService(AuctionReadModel auctionReadModel) {
        this.auctionReadModel = Objects.requireNonNull(auctionReadModel, "auctionReadModel must not be null");
    }

    @Override
    public AuctionBrowsePage browseAuctions(BrowseAuctionsQuery query) {
        return auctionReadModel.browseAuctions(query);
    }

    @Override
    public AuctionBrowsePage browseSellerAuctions(PartyId sellerId, BrowseAuctionsQuery query) {
        return auctionReadModel.browseSellerAuctions(sellerId, query);
    }

    @Override
    public AuctionBrowsePage browseBidderAuctions(PartyId bidderId, BrowseAuctionsQuery query) {
        return auctionReadModel.browseBidderAuctions(bidderId, query);
    }
}
