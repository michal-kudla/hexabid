package com.github.hexabid.product;

import java.util.List;

public record AndRule(List<SelectionRule> rules) implements SelectionRule {

    public AndRule {
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("rules must not be null or empty");
        }
        rules = List.copyOf(rules);
    }

    @Override
    public boolean isSatisfiedBy(List<SelectedProduct> selection) {
        return rules.stream().allMatch(r -> r.isSatisfiedBy(selection));
    }

    @Override
    public String toString() {
        return "AND(%d rules)".formatted(rules.size());
    }
}
