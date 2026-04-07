package com.acme.auctions.product;

import java.util.List;
import java.util.Objects;

/**
 * PackageType represents a product composed of other products.
 * This is the composite in the composite pattern.
 */
public final class PackageType implements Product {

    private final ProductIdentifier id;
    private final ProductName name;
    private final ProductDescription description;
    private final ProductTrackingStrategy trackingStrategy;
    private final ProductMetadata metadata;
    private final ApplicabilityConstraint applicabilityConstraint;
    private final PackageStructure structure;

    PackageType(ProductIdentifier id,
                ProductName name,
                ProductDescription description,
                ProductTrackingStrategy trackingStrategy,
                ProductMetadata metadata,
                ApplicabilityConstraint applicabilityConstraint,
                PackageStructure structure) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(trackingStrategy, "trackingStrategy must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(applicabilityConstraint, "applicabilityConstraint must not be null");
        Objects.requireNonNull(structure, "structure must not be null");
        this.id = id;
        this.name = name;
        this.description = description;
        this.trackingStrategy = trackingStrategy;
        this.metadata = metadata;
        this.applicabilityConstraint = applicabilityConstraint;
        this.structure = structure;
    }

    /**
     * Creates a package with default settings.
     */
    public static PackageType define(ProductIdentifier id,
                                     ProductName name,
                                     ProductDescription description,
                                     PackageStructure structure) {
        return new PackageType(id, name, description,
            ProductTrackingStrategy.INDIVIDUALLY_TRACKED,
            ProductMetadata.empty(),
            ApplicabilityConstraint.alwaysTrue(),
            structure);
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

    public ProductTrackingStrategy trackingStrategy() {
        return trackingStrategy;
    }

    public PackageStructure structure() {
        return structure;
    }

    @Override
    public ProductMetadata metadata() {
        return metadata;
    }

    @Override
    public ApplicabilityConstraint applicabilityConstraint() {
        return applicabilityConstraint;
    }

    /**
     * Validates if selected products match package structure rules.
     */
    public boolean validateSelection(List<SelectedProduct> selection) {
        return structure.validate(selection);
    }

    @Override
    public String toString() {
        return "PackageType{id=%s, name=%s, tracking=%s, structure=%s}".formatted(
            id, name, trackingStrategy, structure);
    }
}
