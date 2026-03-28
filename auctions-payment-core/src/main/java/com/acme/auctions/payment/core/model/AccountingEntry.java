package com.acme.auctions.payment.core.model;

import com.acme.auctions.core.auctioning.model.Price;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single debit or credit entry in an account.
 */
public record AccountingEntry(
        EntryId id,
        AccountId accountId,
        Price amount,
        EntryType type,
        Instant occurredAt
) {
    public AccountingEntry {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    public enum EntryType {
        DEBIT, CREDIT
    }
}
