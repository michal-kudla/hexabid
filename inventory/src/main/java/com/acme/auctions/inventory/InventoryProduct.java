package com.acme.auctions.inventory;

import com.acme.auctions.product.ProductIdentifier;

import java.util.Objects;

public record InventoryProduct(ProductIdentifier productId) {

    public InventoryProduct {
        Objects.requireNonNull(productId, "productId must not be null");
    }
}
