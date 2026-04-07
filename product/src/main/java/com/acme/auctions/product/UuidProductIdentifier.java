package com.acme.auctions.product;

import java.util.UUID;

public record UuidProductIdentifier(UUID value) implements ProductIdentifier {
    public UuidProductIdentifier {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
    }
}
