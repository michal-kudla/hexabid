package com.github.hexabid.payment.core.model;

import java.util.UUID;

public record EntryId(UUID value) {
    public static EntryId next() { return new EntryId(UUID.randomUUID()); }
}
