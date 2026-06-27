package com.github.hexabid.adapter.out.kyc.local;

import com.github.hexabid.core.auctioning.port.out.KycClient;
import com.github.hexabid.core.party.model.PartyId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class DevKycClientAdapter implements KycClient {

    @Override
    public boolean isVerified(PartyId partyId) {
        String idVal = partyId.value();
        if ("dev:bidder-lena".equals(idVal)) {
            return false;
        }
        return idVal.startsWith("dev:") || idVal.startsWith("local:");
    }
}
