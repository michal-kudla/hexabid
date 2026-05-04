package com.github.hexabid.adapter.out.kyc.local;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.port.out.WadiumDepositPort;
import com.github.hexabid.core.party.model.PartyId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Primary
@Component
public class DevWadiumDepositAdapter implements WadiumDepositPort {

    private final Map<String, Boolean> payments = new ConcurrentHashMap<>();

    @Override
    public boolean hasPaidWadium(PartyId partyId, AuctionId auctionId) {
        return payments.getOrDefault(key(partyId, auctionId), false);
    }

    @Override
    public void registerWadiumPayment(PartyId partyId, AuctionId auctionId) {
        payments.put(key(partyId, auctionId), true);
    }

    @Override
    public void refundWadium(PartyId partyId, AuctionId auctionId) {
        payments.remove(key(partyId, auctionId));
    }

    private static String key(PartyId partyId, AuctionId auctionId) {
        return partyId.value() + ":" + auctionId.value();
    }
}
