package com.acme.auctions.payment.core.model;

import java.util.UUID;

public record TransactionId(UUID value) {
    public static TransactionId next() { return new TransactionId(UUID.randomUUID()); }
}
