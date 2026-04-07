package com.acme.auctions.product;

public record GtinProductIdentifier(String value) implements ProductIdentifier {
    public GtinProductIdentifier {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be null or blank");
        }
    }
}
