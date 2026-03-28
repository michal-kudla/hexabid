package com.acme.auctions.payment.core.model;

import com.acme.auctions.core.party.model.PartyId;
import com.acme.auctions.core.auctioning.model.Price;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a financial account in the Accounting archetype.
 */
public record Account(
        AccountId id,
        PartyId ownerId,
        String currency
) {
    public Account {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
    }
}
