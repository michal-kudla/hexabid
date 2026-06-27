package com.github.hexabid.product;

import java.util.Arrays;
import java.util.List;

/**
 * SelectionRule defines constraints about how products can be selected in a package.
 */
public sealed interface SelectionRule permits IsSubsetOf, AndRule, OrRule, NotRule, ConditionalRule {

    boolean isSatisfiedBy(List<SelectedProduct> selection);

    static SelectionRule isSubsetOf(ProductSet sourceSet, int min, int max) {
        return new IsSubsetOf(sourceSet, min, max);
    }

    static SelectionRule single(ProductSet sourceSet) {
        return new IsSubsetOf(sourceSet, 1, 1);
    }

    static SelectionRule optional(ProductSet sourceSet) {
        return new IsSubsetOf(sourceSet, 0, 1);
    }

    static SelectionRule required(ProductSet sourceSet) {
        return new IsSubsetOf(sourceSet, 1, Integer.MAX_VALUE);
    }

    static SelectionRule and(SelectionRule... rules) {
        return new AndRule(List.of(rules));
    }

    static SelectionRule or(SelectionRule... rules) {
        return new OrRule(List.of(rules));
    }

    static SelectionRule ifThen(SelectionRule condition, SelectionRule... thenRules) {
        return new ConditionalRule(condition, List.of(thenRules));
    }

    static SelectionRule not(SelectionRule rule) {
        return new NotRule(rule);
    }
}
