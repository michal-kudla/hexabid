package com.github.hexabid.product;

import java.util.List;
import java.util.Objects;

/**
 * PackageStructure defines what products can be selected in a package and the rules for selection.
 */
public record PackageStructure(List<ProductSet> productSets, List<SelectionRule> selectionRules) {

    public PackageStructure {
        Objects.requireNonNull(productSets, "productSets must not be null");
        Objects.requireNonNull(selectionRules, "selectionRules must not be null");
        productSets = List.copyOf(productSets);
        selectionRules = List.copyOf(selectionRules);
    }

    public static PackageStructure empty() {
        return new PackageStructure(List.of(), List.of());
    }

    /**
     * Validates if selected products match package structure rules.
     */
    public boolean validate(List<SelectedProduct> selection) {
        return selectionRules.stream().allMatch(rule -> rule.isSatisfiedBy(selection));
    }
}
