package com.acme.auctions.product;

import java.util.List;

public record IsSubsetOf(ProductSet sourceSet, int min, int max) implements SelectionRule {

    public IsSubsetOf {
        if (sourceSet == null) {
            throw new IllegalArgumentException("sourceSet must not be null");
        }
        if (min < 0) {
            throw new IllegalArgumentException("min must be >= 0");
        }
        if (max < min) {
            throw new IllegalArgumentException("max must be >= min");
        }
    }

    @Override
    public boolean isSatisfiedBy(List<SelectedProduct> selection) {
        long count = selection.stream()
            .filter(s -> sourceSet.contains(s.productId()))
            .mapToInt(SelectedProduct::quantity)
            .sum();
        return count >= min && count <= max;
    }

    @Override
    public String toString() {
        return "IsSubsetOf{set='%s', min=%d, max=%d}".formatted(sourceSet.name(), min, max);
    }
}
