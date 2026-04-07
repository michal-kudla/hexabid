package com.acme.auctions.product;

import java.util.List;

public record OrRule(List<SelectionRule> rules) implements SelectionRule {

    public OrRule {
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("rules must not be null or empty");
        }
        rules = List.copyOf(rules);
    }

    @Override
    public boolean isSatisfiedBy(List<SelectedProduct> selection) {
        return rules.stream().anyMatch(r -> r.isSatisfiedBy(selection));
    }

    @Override
    public String toString() {
        return "OR(%d rules)".formatted(rules.size());
    }
}
