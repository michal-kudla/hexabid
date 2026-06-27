package com.github.hexabid.payment.core.model;

import com.github.hexabid.core.auctioning.model.Price;
import java.math.BigDecimal;
import java.util.*;

public final class Ledger {

    private final ChartOfAccounts chartOfAccounts;
    private final List<AccountingTransaction> transactions;

    public Ledger(ChartOfAccounts chartOfAccounts) {
        this.chartOfAccounts = Objects.requireNonNull(chartOfAccounts, "chartOfAccounts must not be null");
        this.transactions = new ArrayList<>();
    }

    public AccountingTransaction post(AccountingTransaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        if (!transaction.isBalanced()) {
            throw new IllegalArgumentException("Cannot post unbalanced transaction");
        }
        for (var entry : transaction.entries()) {
            if (chartOfAccounts.findById(entry.accountId()).isEmpty()) {
                throw new IllegalArgumentException("Account not found: " + entry.accountId());
            }
        }
        transactions.add(transaction);
        return transaction;
    }

    public Price getBalance(AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        var account = chartOfAccounts.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        BigDecimal balance = BigDecimal.ZERO;
        for (var tx : transactions) {
            for (var entry : tx.entries()) {
                if (entry.accountId().equals(accountId)) {
                    if (entry.type() == AccountingEntry.EntryType.DEBIT) {
                        balance = balance.add(entry.amount().amount());
                    } else {
                        balance = balance.subtract(entry.amount().amount());
                    }
                }
            }
        }
        return new Price(balance, account.currency());
    }

    public List<AccountingTransaction> getTransactions() {
        return List.copyOf(transactions);
    }

    public List<AccountingTransaction> getTransactionsForAccount(AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return transactions.stream()
                .filter(tx -> tx.entries().stream().anyMatch(e -> e.accountId().equals(accountId)))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Ledger ledger
                && chartOfAccounts.equals(ledger.chartOfAccounts)
                && transactions.equals(ledger.transactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chartOfAccounts, transactions);
    }

    @Override
    public String toString() {
        return "Ledger{" + transactions.size() + " transactions}";
    }
}
