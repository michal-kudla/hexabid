package com.acme.auctions.product;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Collection of feature type definitions belonging to a ProductType.
 */
public record ProductFeatureTypes(List<ProductFeatureType> types) {

    public ProductFeatureTypes {
        if (types == null) {
            throw new IllegalArgumentException("types must not be null");
        }
        types = List.copyOf(types);
        validateUniqueNames(types);
    }

    public static ProductFeatureTypes empty() {
        return new ProductFeatureTypes(List.of());
    }

    public static ProductFeatureTypes of(ProductFeatureType... types) {
        return new ProductFeatureTypes(List.of(types));
    }

    public Optional<ProductFeatureType> findByName(String name) {
        return types.stream()
            .filter(t -> t.name().equals(name))
            .findFirst();
    }

    /**
     * Validates all feature instances against their type definitions.
     */
    public void validateInstances(List<ProductFeatureInstance> instances) {
        Map<String, ProductFeatureType> typeMap = types.stream()
            .collect(Collectors.toMap(ProductFeatureType::name, t -> t));

        for (ProductFeatureInstance instance : instances) {
            ProductFeatureType type = typeMap.get(instance.type().name());
            if (type == null) {
                throw new IllegalArgumentException(
                    "Unknown feature type: " + instance.type().name());
            }
            type.validate(instance.value());
        }
    }

    private static void validateUniqueNames(List<ProductFeatureType> types) {
        Set<String> names = types.stream()
            .map(ProductFeatureType::name)
            .collect(Collectors.toSet());
        if (names.size() != types.size()) {
            throw new IllegalArgumentException("Feature type names must be unique");
        }
    }
}
