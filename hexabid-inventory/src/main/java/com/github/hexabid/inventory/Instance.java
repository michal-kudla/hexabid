package com.github.hexabid.inventory;

import com.github.hexabid.product.BatchId;
import com.github.hexabid.product.ProductIdentifier;
import com.github.hexabid.product.SerialNumber;
import com.github.hexabid.quantity.Quantity;

import java.util.Optional;

/**
 * Instance represents a specific physical exemplar of a Product.
 * While ProductType defines WHAT can be sold, Instance represents WHAT WAS actually created/stored.
 */
public interface Instance {

    InstanceId id();

    ProductIdentifier productId();

    Optional<SerialNumber> maybeSerialNumber();

    Optional<BatchId> maybeBatchId();

    Optional<Quantity> maybeQuantity();

    default Quantity effectiveQuantity() {
        return maybeQuantity().orElse(com.github.hexabid.quantity.Quantity.of(1, com.github.hexabid.quantity.Unit.pieces()));
    }
}
