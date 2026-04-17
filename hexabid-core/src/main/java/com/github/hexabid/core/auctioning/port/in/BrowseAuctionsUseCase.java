package com.github.hexabid.core.auctioning.port.in;

import com.github.hexabid.core.party.model.PartyId;

public interface BrowseAuctionsUseCase {
    AuctionBrowsePage browseAuctions(BrowseAuctionsQuery query);

    AuctionBrowsePage browseSellerAuctions(PartyId sellerId, BrowseAuctionsQuery query);

    AuctionBrowsePage browseBidderAuctions(PartyId bidderId, BrowseAuctionsQuery query);
}
