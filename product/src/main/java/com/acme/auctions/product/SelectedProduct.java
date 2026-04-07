package com.acme.auctions.product;

/**
 * Represents a selected product in a package with its quantity.
 */
public record SelectedProduct(ProductIdentifier productId, int quantity) {

    public SelectedProduct {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }
    }
}
