package com.acme.auctions.product;

import com.acme.auctions.quantity.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for ProductType and PackageType.
 */
public class ProductBuilder {

    private final ProductIdentifier id;
    private final ProductName name;
    private final ProductDescription description;
    private Unit preferredUnit;
    private ProductTrackingStrategy trackingStrategy;
    private ProductFeatureTypes featureTypes;
    private ProductMetadata metadata;
    private ApplicabilityConstraint applicabilityConstraint;

    ProductBuilder(ProductIdentifier id, ProductName name, ProductDescription description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.preferredUnit = Unit.pieces();
        this.trackingStrategy = ProductTrackingStrategy.IDENTICAL;
        this.featureTypes = ProductFeatureTypes.empty();
        this.metadata = ProductMetadata.empty();
        this.applicabilityConstraint = ApplicabilityConstraint.alwaysTrue();
    }

    public ProductTypeBuilder asProductType(Unit preferredUnit, ProductTrackingStrategy trackingStrategy) {
        this.preferredUnit = preferredUnit;
        this.trackingStrategy = trackingStrategy;
        return new ProductTypeBuilder(this);
    }

    public PackageTypeBuilder asPackageType(ProductTrackingStrategy trackingStrategy) {
        this.trackingStrategy = trackingStrategy;
        return new PackageTypeBuilder(this);
    }

    ProductIdentifier id() { return id; }
    ProductName name() { return name; }
    ProductDescription description() { return description; }
    Unit preferredUnit() { return preferredUnit; }
    ProductTrackingStrategy trackingStrategy() { return trackingStrategy; }
    ProductFeatureTypes featureTypes() { return featureTypes; }
    ProductMetadata metadata() { return metadata; }
    ApplicabilityConstraint applicabilityConstraint() { return applicabilityConstraint; }

    ProductBuilder featureTypes(ProductFeatureTypes featureTypes) {
        this.featureTypes = featureTypes;
        return this;
    }

    ProductBuilder metadata(ProductMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    ProductBuilder applicabilityConstraint(ApplicabilityConstraint applicabilityConstraint) {
        this.applicabilityConstraint = applicabilityConstraint;
        return this;
    }

    public static class ProductTypeBuilder {
        private final ProductBuilder parent;

        ProductTypeBuilder(ProductBuilder parent) {
            this.parent = parent;
        }

        public ProductTypeBuilder featureTypes(ProductFeatureTypes featureTypes) {
            parent.featureTypes(featureTypes);
            return this;
        }

        public ProductTypeBuilder metadata(ProductMetadata metadata) {
            parent.metadata(metadata);
            return this;
        }

        public ProductTypeBuilder applicabilityConstraint(ApplicabilityConstraint applicabilityConstraint) {
            parent.applicabilityConstraint(applicabilityConstraint);
            return this;
        }

        public ProductType build() {
            return new ProductType(
                parent.id(), parent.name(), parent.description(),
                parent.preferredUnit(), parent.trackingStrategy(),
                parent.featureTypes(), parent.metadata(),
                parent.applicabilityConstraint());
        }
    }

    public static class PackageTypeBuilder {
        private final ProductBuilder parent;
        private PackageStructure structure;

        PackageTypeBuilder(ProductBuilder parent) {
            this.parent = parent;
        }

        public PackageTypeBuilder structure(PackageStructure structure) {
            this.structure = structure;
            return this;
        }

        public PackageTypeBuilder metadata(ProductMetadata metadata) {
            parent.metadata(metadata);
            return this;
        }

        public PackageTypeBuilder applicabilityConstraint(ApplicabilityConstraint applicabilityConstraint) {
            parent.applicabilityConstraint(applicabilityConstraint);
            return this;
        }

        public PackageType build() {
            if (structure == null) {
                throw new IllegalStateException("PackageStructure must be set before building");
            }
            return new PackageType(
                parent.id(), parent.name(), parent.description(),
                parent.trackingStrategy(), parent.metadata(),
                parent.applicabilityConstraint(), structure);
        }
    }
}
