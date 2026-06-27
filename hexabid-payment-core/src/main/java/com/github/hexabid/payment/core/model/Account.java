package com.github.hexabid.payment.core.model;

import com.github.hexabid.core.party.model.PartyId;
import java.util.Objects;

public record Account(
        AccountId id,
        PartyId ownerId,
        String currency,
        AccountType type
) {
    public Account {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }
}
