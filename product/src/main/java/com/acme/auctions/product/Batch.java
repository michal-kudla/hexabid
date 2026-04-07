package com.acme.auctions.product;

import com.acme.auctions.quantity.Quantity;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Batch describes a set of ProductInstances of a specific ProductType
 * that are tracked together, usually for quality control purposes.
 */
public class Batch {

    private final BatchId id;
    private final BatchName name;
    private final ProductIdentifier batchOf;
    private final Quantity quantityInBatch;
    private final Instant dateProduced;
    private final Instant sellBy;
    private final Instant useBy;
    private final Instant bestBefore;
    private final SerialNumber startSerialNumber;
    private final SerialNumber endSerialNumber;
    private final String comments;

    private Batch(BatchId id,
                  BatchName name,
                  ProductIdentifier batchOf,
                  Quantity quantityInBatch,
                  Instant dateProduced,
                  Instant sellBy,
                  Instant useBy,
                  Instant bestBefore,
                  SerialNumber startSerialNumber,
                  SerialNumber endSerialNumber,
                  String comments) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(batchOf, "batchOf must not be null");
        Objects.requireNonNull(quantityInBatch, "quantityInBatch must not be null");
        this.id = id;
        this.name = name;
        this.batchOf = batchOf;
        this.quantityInBatch = quantityInBatch;
        this.dateProduced = dateProduced;
        this.sellBy = sellBy;
        this.useBy = useBy;
        this.bestBefore = bestBefore;
        this.startSerialNumber = startSerialNumber;
        this.endSerialNumber = endSerialNumber;
        this.comments = comments;
    }

    public static Builder builder() {
        return new Builder();
    }

    public BatchId id() { return id; }
    public BatchName name() { return name; }
    public ProductIdentifier batchOf() { return batchOf; }
    public Quantity quantityInBatch() { return quantityInBatch; }
    public Optional<Instant> dateProduced() { return Optional.ofNullable(dateProduced); }
    public Optional<Instant> sellBy() { return Optional.ofNullable(sellBy); }
    public Optional<Instant> useBy() { return Optional.ofNullable(useBy); }
    public Optional<Instant> bestBefore() { return Optional.ofNullable(bestBefore); }
    public Optional<SerialNumber> startSerialNumber() { return Optional.ofNullable(startSerialNumber); }
    public Optional<SerialNumber> endSerialNumber() { return Optional.ofNullable(endSerialNumber); }
    public Optional<String> comments() { return Optional.ofNullable(comments); }

    @Override
    public String toString() {
        return "Batch{id=%s, name=%s, of=%s, quantity=%s}".formatted(id, name, batchOf, quantityInBatch);
    }

    public static class Builder {
        private BatchId id;
        private BatchName name;
        private ProductIdentifier batchOf;
        private Quantity quantityInBatch;
        private Instant dateProduced;
        private Instant sellBy;
        private Instant useBy;
        private Instant bestBefore;
        private SerialNumber startSerialNumber;
        private SerialNumber endSerialNumber;
        private String comments;

        public Builder id(BatchId id) { this.id = id; return this; }
        public Builder name(BatchName name) { this.name = name; return this; }
        public Builder batchOf(ProductIdentifier batchOf) { this.batchOf = batchOf; return this; }
        public Builder quantityInBatch(Quantity quantityInBatch) { this.quantityInBatch = quantityInBatch; return this; }
        public Builder dateProduced(Instant dateProduced) { this.dateProduced = dateProduced; return this; }
        public Builder sellBy(Instant sellBy) { this.sellBy = sellBy; return this; }
        public Builder useBy(Instant useBy) { this.useBy = useBy; return this; }
        public Builder bestBefore(Instant bestBefore) { this.bestBefore = bestBefore; return this; }
        public Builder startSerialNumber(SerialNumber startSerialNumber) { this.startSerialNumber = startSerialNumber; return this; }
        public Builder endSerialNumber(SerialNumber endSerialNumber) { this.endSerialNumber = endSerialNumber; return this; }
        public Builder comments(String comments) { this.comments = comments; return this; }

        public Batch build() {
            return new Batch(id, name, batchOf, quantityInBatch, dateProduced, sellBy, useBy, bestBefore,
                startSerialNumber, endSerialNumber, comments);
        }
    }
}
