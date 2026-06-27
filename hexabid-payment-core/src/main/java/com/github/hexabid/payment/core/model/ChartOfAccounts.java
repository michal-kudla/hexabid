package com.github.hexabid.payment.core.model;

import java.util.*;

public final class ChartOfAccounts {

    private final Map<AccountId, Account> accounts;

    public ChartOfAccounts() {
        this.accounts = new HashMap<>();
    }

    public ChartOfAccounts(Collection<Account> accounts) {
        this.accounts = new HashMap<>();
        for (var account : accounts) {
            register(account);
        }
    }

    public void register(Account account) {
        Objects.requireNonNull(account, "account must not be null");
        if (accounts.containsKey(account.id())) {
            throw new IllegalArgumentException("Account already registered: " + account.id());
        }
        accounts.put(account.id(), account);
    }

    public Optional<Account> findById(AccountId id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(accounts.get(id));
    }

    public List<Account> findByOwner(com.github.hexabid.core.party.model.PartyId ownerId) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        return accounts.values().stream()
                .filter(a -> ownerId.equals(a.ownerId()))
                .toList();
    }

    public List<Account> findByType(AccountType type) {
        Objects.requireNonNull(type, "type must not be null");
        return accounts.values().stream()
                .filter(a -> type.equals(a.type()))
                .toList();
    }

    public List<Account> all() {
        return List.copyOf(accounts.values());
    }

    public int size() {
        return accounts.size();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ChartOfAccounts that && accounts.equals(that.accounts);
    }

    @Override
    public int hashCode() {
        return accounts.hashCode();
    }

    @Override
    public String toString() {
        return "ChartOfAccounts{" + accounts.size() + " accounts}";
    }
}
