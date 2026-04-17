package com.github.hexabid.product;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ProductSet represents a named collection of products available for selection in a package.
 */
public record ProductSet(String name, Set<ProductIdentifier> products) {

    public ProductSet {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(products, "products must not be null");
        if (products.isEmpty()) {
            throw new IllegalArgumentException("products must not be empty");
        }
        products = Set.copyOf(products);
    }

    public static ProductSet singleOf(String name, ProductIdentifier id) {
        return new ProductSet(name, Set.of(id));
    }

    public static ProductSet of(String name, ProductIdentifier... ids) {
        return new ProductSet(name, Arrays.stream(ids).collect(Collectors.toSet()));
    }

    public boolean contains(ProductIdentifier productId) {
        return products.contains(productId);
    }

    @Override
    public String toString() {
        return "ProductSet{name='%s', products=%s}".formatted(name, products);
    }
}
