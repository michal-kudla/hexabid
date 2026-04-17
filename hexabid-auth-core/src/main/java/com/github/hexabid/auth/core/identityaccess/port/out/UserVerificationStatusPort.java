package com.github.hexabid.auth.core.identityaccess.port.out;

import com.github.hexabid.core.party.model.PartyId;

public interface UserVerificationStatusPort {
    boolean isVerified(PartyId partyId);
}
