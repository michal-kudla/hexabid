package com.github.hexabid.product;

import java.util.List;

public record NotRule(SelectionRule rule) implements SelectionRule {

    public NotRule {
        if (rule == null) {
            throw new IllegalArgumentException("rule must not be null");
        }
    }

    @Override
    public boolean isSatisfiedBy(List<SelectedProduct> selection) {
        return !rule.isSatisfiedBy(selection);
    }

    @Override
    public String toString() {
        return "NOT(%s)".formatted(rule);
    }
}
