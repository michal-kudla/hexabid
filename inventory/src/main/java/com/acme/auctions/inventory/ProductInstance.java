package com.acme.auctions.inventory;

import com.acme.auctions.product.BatchId;
import com.acme.auctions.product.ProductIdentifier;
import com.acme.auctions.product.SerialNumber;
import com.acme.auctions.quantity.Quantity;
import com.acme.auctions.quantity.Unit;

import java.util.Objects;
import java.util.Optional;

public class ProductInstance implements Instance {

    private final InstanceId id;
    private final ProductIdentifier productId;
    private final SerialNumber serialNumber;
    private final BatchId batchId;
    private final Quantity quantity;

    private ProductInstance(InstanceId id,
                            ProductIdentifier productId,
                            SerialNumber serialNumber,
                            BatchId batchId,
                            Quantity quantity) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(productId, "productId must not be null");
        this.id = id;
        this.productId = productId;
        this.serialNumber = serialNumber;
        this.batchId = batchId;
        this.quantity = quantity;
    }

    public static ProductInstance unique(InstanceId id, ProductIdentifier productId, SerialNumber serialNumber) {
        return new ProductInstance(id, productId, serialNumber, null, null);
    }

    public static ProductInstance batched(InstanceId id, ProductIdentifier productId, BatchId batchId, Quantity quantity) {
        return new ProductInstance(id, productId, null, batchId, quantity);
    }

    public static ProductInstance identical(InstanceId id, ProductIdentifier productId, Quantity quantity) {
        return new ProductInstance(id, productId, null, null, quantity);
    }

    @Override
    public InstanceId id() {
        return id;
    }

    @Override
    public ProductIdentifier productId() {
        return productId;
    }

    @Override
    public Optional<SerialNumber> maybeSerialNumber() {
        return Optional.ofNullable(serialNumber);
    }

    @Override
    public Optional<BatchId> maybeBatchId() {
        return Optional.ofNullable(batchId);
    }

    @Override
    public Optional<Quantity> maybeQuantity() {
        return Optional.ofNullable(quantity);
    }

    @Override
    public Quantity effectiveQuantity() {
        return quantity != null ? quantity : Quantity.of(1, Unit.pieces());
    }

    @Override
    public String toString() {
        return "ProductInstance{id=%s, productId=%s, serial=%s, batch=%s, quantity=%s}".formatted(
            id, productId,
            serialNumber != null ? serialNumber : "none",
            batchId != null ? batchId : "none",
            quantity != null ? quantity : "implicit 1 pcs");
    }
}
