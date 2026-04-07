package com.acme.auctions.product;

import java.util.Objects;

/**
 * ProductType represents a type/definition of a product (not a specific instance).
 * Examples: "iPhone 15 Pro 256GB", "Clean Code book", "Organic Milk 1L"
 *
 * Each ProductType is uniquely identified by a ProductIdentifier (UUID, GTIN, etc.)
 * and defines how instances should be tracked and measured.
 *
 * This is a leaf in the composite pattern - a regular product (not a package).
 */
public final class ProductType implements Product {

    private final ProductIdentifier id;
    private final ProductName name;
    private final ProductDescription description;
    private final com.acme.auctions.quantity.Unit preferredUnit;
    private final ProductTrackingStrategy trackingStrategy;
    private final ProductFeatureTypes featureTypes;
    private final ProductMetadata metadata;
    private final ApplicabilityConstraint applicabilityConstraint;

    ProductType(ProductIdentifier id,
                ProductName name,
                ProductDescription description,
                com.acme.auctions.quantity.Unit preferredUnit,
                ProductTrackingStrategy trackingStrategy,
                ProductFeatureTypes featureTypes,
                ProductMetadata metadata,
                ApplicabilityConstraint applicabilityConstraint) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(preferredUnit, "preferredUnit must not be null");
        Objects.requireNonNull(trackingStrategy, "trackingStrategy must not be null");
        Objects.requireNonNull(featureTypes, "featureTypes must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(applicabilityConstraint, "applicabilityConstraint must not be null");
        this.id = id;
        this.name = name;
        this.description = description;
        this.preferredUnit = preferredUnit;
        this.trackingStrategy = trackingStrategy;
        this.featureTypes = featureTypes;
        this.metadata = metadata;
        this.applicabilityConstraint = applicabilityConstraint;
    }

    /**
     * Creates a simple product type for testing purposes.
     */
    public static ProductType define(ProductIdentifier id,
                                     ProductName name,
                                     ProductDescription description) {
        return new ProductType(id, name, description, com.acme.auctions.quantity.Unit.pieces(),
            ProductTrackingStrategy.IDENTICAL, ProductFeatureTypes.empty(),
            ProductMetadata.empty(), ApplicabilityConstraint.alwaysTrue());
    }

    /**
     * Creates a unique product type (one-of-a-kind).
     */
    public static ProductType unique(ProductIdentifier id,
                                     ProductName name,
                                     ProductDescription description) {
        return new ProductType(id, name, description, com.acme.auctions.quantity.Unit.pieces(),
            ProductTrackingStrategy.UNIQUE, ProductFeatureTypes.empty(),
            ProductMetadata.empty(), ApplicabilityConstraint.alwaysTrue());
    }

    /**
     * Creates a product type where each instance is individually tracked.
     */
    public static ProductType individuallyTracked(ProductIdentifier id,
                                                  ProductName name,
                                                  ProductDescription description,
                                                  com.acme.auctions.quantity.Unit preferredUnit) {
        return new ProductType(id, name, description, preferredUnit,
            ProductTrackingStrategy.INDIVIDUALLY_TRACKED, ProductFeatureTypes.empty(),
            ProductMetadata.empty(), ApplicabilityConstraint.alwaysTrue());
    }

    /**
     * Creates a product type where instances are tracked by production batch.
     */
    public static ProductType batchTracked(ProductIdentifier id,
                                           ProductName name,
                                           ProductDescription description,
                                           com.acme.auctions.quantity.Unit preferredUnit) {
        return new ProductType(id, name, description, preferredUnit,
            ProductTrackingStrategy.BATCH_TRACKED, ProductFeatureTypes.empty(),
            ProductMetadata.empty(), ApplicabilityConstraint.alwaysTrue());
    }

    /**
     * Creates a product type where instances are tracked both individually and by batch.
     */
    public static ProductType individuallyAndBatchTracked(ProductIdentifier id,
                                                          ProductName name,
                                                          ProductDescription description,
                                                          com.acme.auctions.quantity.Unit preferredUnit) {
        return new ProductType(id, name, description, preferredUnit,
            ProductTrackingStrategy.INDIVIDUALLY_AND_BATCH_TRACKED, ProductFeatureTypes.empty(),
            ProductMetadata.empty(), ApplicabilityConstraint.alwaysTrue());
    }

    /**
     * Creates a product type where instances are interchangeable (identical).
     */
    public static ProductType identical(ProductIdentifier id,
                                        ProductName name,
                                        ProductDescription description,
                                        com.acme.auctions.quantity.Unit preferredUnit) {
        return new ProductType(id, name, description, preferredUnit,
            ProductTrackingStrategy.IDENTICAL, ProductFeatureTypes.empty(),
            ProductMetadata.empty(), ApplicabilityConstraint.alwaysTrue());
    }

    @Override
    public ProductIdentifier id() {
        return id;
    }

    @Override
    public ProductName name() {
        return name;
    }

    @Override
    public ProductDescription description() {
        return description;
    }

    public com.acme.auctions.quantity.Unit preferredUnit() {
        return preferredUnit;
    }

    public ProductTrackingStrategy trackingStrategy() {
        return trackingStrategy;
    }

    public ProductFeatureTypes featureTypes() {
        return featureTypes;
    }

    @Override
    public ProductMetadata metadata() {
        return metadata;
    }

    @Override
    public ApplicabilityConstraint applicabilityConstraint() {
        return applicabilityConstraint;
    }

    @Override
    public String toString() {
        return "ProductType{id=%s, name=%s, unit=%s, tracking=%s, features=%s}".formatted(
            id, name, preferredUnit, trackingStrategy, featureTypes);
    }
}
