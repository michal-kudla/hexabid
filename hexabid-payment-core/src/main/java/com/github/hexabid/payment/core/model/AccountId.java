package com.github.hexabid.payment.core.model;

import java.util.UUID;

public record AccountId(UUID value) {
    public static AccountId next() { return new AccountId(UUID.randomUUID()); }
}
