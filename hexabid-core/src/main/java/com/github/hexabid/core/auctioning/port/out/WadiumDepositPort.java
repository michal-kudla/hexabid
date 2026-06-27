package com.github.hexabid.core.auctioning.port.out;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.party.model.PartyId;

public interface WadiumDepositPort {
    boolean hasPaidWadium(PartyId partyId, AuctionId auctionId);

    void registerWadiumPayment(PartyId partyId, AuctionId auctionId);

    void refundWadium(PartyId partyId, AuctionId auctionId);
}
