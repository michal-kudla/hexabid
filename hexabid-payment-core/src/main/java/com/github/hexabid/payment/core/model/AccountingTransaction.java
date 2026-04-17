package com.github.hexabid.payment.core.model;

import com.github.hexabid.core.auctioning.model.AuctionId;
import java.util.List;
import java.util.Objects;

/**
 * Represents a group of accounting entries that form a logical transaction.
 */
public record AccountingTransaction(
        TransactionId id,
        AuctionId auctionId,
        List<AccountingEntry> entries
) {
    public AccountingTransaction {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(entries, "entries must not be null");
    }

    public boolean isBalanced() {
        // Simple balance check could be added here if needed for more complex accounting
        return true; 
    }
}
