package com.acme.auctions.inventory;

import com.acme.auctions.product.BatchId;
import com.acme.auctions.product.ProductIdentifier;
import com.acme.auctions.product.SerialNumber;
import com.acme.auctions.quantity.Quantity;

import java.util.Optional;

/**
 * Instance represents a specific physical exemplar of a Product.
 * While ProductType defines WHAT can be sold, Instance represents WHAT WAS actually created/stored.
 */
public interface Instance {

    InstanceId id();

    ProductIdentifier productId();

    Optional<SerialNumber> serialNumber();

    Optional<BatchId> batchId();

    Optional<Quantity> quantity();

    default Quantity effectiveQuantity() {
        return quantity().orElse(com.acme.auctions.quantity.Quantity.of(1, com.acme.auctions.quantity.Unit.pieces()));
    }
}
