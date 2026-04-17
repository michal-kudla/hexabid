package com.github.hexabid.inventory;

import com.github.hexabid.product.ProductIdentifier;

import java.util.Objects;

public record InventoryProduct(ProductIdentifier productId) {

    public InventoryProduct {
        Objects.requireNonNull(productId, "productId must not be null");
    }
}
