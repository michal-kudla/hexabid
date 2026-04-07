package com.acme.auctions.product;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Concrete feature instance — actual value for a ProductFeatureType.
 */
public record ProductFeatureInstance(ProductFeatureType type, Object value) {

    public ProductFeatureInstance {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(value, "value must not be null");
        type.validate(value);
    }
}
