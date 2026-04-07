package com.acme.auctions.product;

import java.util.List;

public record ConditionalRule(SelectionRule condition, List<SelectionRule> thenRules) implements SelectionRule {

    public ConditionalRule {
        if (condition == null) {
            throw new IllegalArgumentException("condition must not be null");
        }
        if (thenRules == null || thenRules.isEmpty()) {
            throw new IllegalArgumentException("thenRules must not be null or empty");
        }
        thenRules = List.copyOf(thenRules);
    }

    @Override
    public boolean isSatisfiedBy(List<SelectedProduct> selection) {
        if (condition.isSatisfiedBy(selection)) {
            return thenRules.stream().allMatch(r -> r.isSatisfiedBy(selection));
        }
        return true;
    }

    @Override
    public String toString() {
        return "IF(%s) THEN(%d rules)".formatted(condition, thenRules.size());
    }
}
