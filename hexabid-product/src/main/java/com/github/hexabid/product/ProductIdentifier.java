package com.github.hexabid.product;

import java.util.UUID;

public sealed interface ProductIdentifier permits UuidProductIdentifier, GtinProductIdentifier {
    static UuidProductIdentifier uuid(UUID value) {
        return new UuidProductIdentifier(value);
    }

    static UuidProductIdentifier randomUuid() {
        return new UuidProductIdentifier(UUID.randomUUID());
    }

    static GtinProductIdentifier gtin(String value) {
        return new GtinProductIdentifier(value);
    }
}
