package com.github.hexabid.payment.core.model;

import com.github.hexabid.core.auctioning.model.AuctionId;
import java.util.List;
import java.util.Objects;

public record AccountingTransaction(
        TransactionId id,
        AuctionId auctionId,
        List<AccountingEntry> entries
) {
    public AccountingTransaction {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(entries, "entries must not be null");
        if (entries.size() < 2) {
            throw new IllegalArgumentException("Transaction must have at least 2 entries (DEBIT and CREDIT)");
        }
    }

    public boolean isBalanced() {
        var debitSum = entries.stream()
                .filter(e -> e.type() == AccountingEntry.EntryType.DEBIT)
                .map(e -> e.amount().amount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        var creditSum = entries.stream()
                .filter(e -> e.type() == AccountingEntry.EntryType.CREDIT)
                .map(e -> e.amount().amount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        return debitSum.compareTo(creditSum) == 0;
    }

    public static AccountingTransaction createBalanced(
            TransactionId id,
            AuctionId auctionId,
            AccountingEntry debit,
            AccountingEntry credit
    ) {
        var tx = new AccountingTransaction(id, auctionId, List.of(debit, credit));
        if (!tx.isBalanced()) {
            throw new IllegalArgumentException("Transaction is not balanced: DEBIT " + debit.amount() + " != CREDIT " + credit.amount());
        }
        return tx;
    }
}
