package com.github.hexabid.core.auctioning.port.out;

import com.github.hexabid.core.auctioning.port.in.AuctionBrowsePage;
import com.github.hexabid.core.auctioning.port.in.BrowseAuctionsQuery;
import com.github.hexabid.core.party.model.PartyId;

public interface AuctionReadModel {
    AuctionBrowsePage browseAuctions(BrowseAuctionsQuery query);

    AuctionBrowsePage browseSellerAuctions(PartyId sellerId, BrowseAuctionsQuery query);

    AuctionBrowsePage browseBidderAuctions(PartyId bidderId, BrowseAuctionsQuery query);
}
