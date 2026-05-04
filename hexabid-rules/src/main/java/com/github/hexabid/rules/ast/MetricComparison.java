package com.github.hexabid.rules.ast;

import java.util.Objects;

public record MetricComparison(AttributeKey metric, AttributeCheck.Comparator comparator, Object threshold) implements Expression {

    public MetricComparison {
        Objects.requireNonNull(metric);
        Objects.requireNonNull(comparator);
        Objects.requireNonNull(threshold);
    }
}
