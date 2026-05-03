package com.github.hexabid.rules.algebra;

import com.github.hexabid.rules.ast.AttributeCheck;
import com.github.hexabid.rules.ast.AttributeKey;
import com.github.hexabid.rules.model.RuleContext;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;

public final class BooleanAlgebra implements RuleAlgebra<Boolean> {

    private final RuleContext context;

    public BooleanAlgebra(RuleContext context) {
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public Boolean and(Boolean left, Boolean right) {
        return left && right;
    }

    @Override
    public Boolean or(Boolean left, Boolean right) {
        return left || right;
    }

    @Override
    public Boolean not(Boolean inner) {
        return !inner;
    }

    @Override
    public Boolean ifThenElse(Boolean condition, Boolean thenResult, Boolean elseResult) {
        return condition ? thenResult : elseResult;
    }

    @Override
    public Boolean constant(boolean value) {
        return value;
    }

    @Override
    public Boolean attributeCheck(AttributeKey key, AttributeCheck.Comparator cmp, Object value) {
        var actual = context.get(key);
        return compare(actual, cmp, value);
    }

    @Override
    public Boolean metricComparison(AttributeKey metric, AttributeCheck.Comparator cmp, Object threshold) {
        var actual = context.get(metric);
        return compare(actual, cmp, threshold);
    }

    private boolean compare(Object actual, AttributeCheck.Comparator cmp, Object expected) {
        if (cmp == AttributeCheck.Comparator.IS_NULL) {
            return actual == null;
        }
        if (cmp == AttributeCheck.Comparator.IS_NOT_NULL) {
            return actual != null;
        }
        if (actual == null) {
            return false;
        }
        return switch (cmp) {
            case EQUALS -> actual.equals(expected);
            case NOT_EQUALS -> !actual.equals(expected);
            case GREATER_THAN -> compareValues(actual, expected) > 0;
            case GREATER_THAN_OR_EQUAL -> compareValues(actual, expected) >= 0;
            case LESS_THAN -> compareValues(actual, expected) < 0;
            case LESS_THAN_OR_EQUAL -> compareValues(actual, expected) <= 0;
            case IN -> expected instanceof Collection<?> col && col.contains(actual);
            case NOT_IN -> expected instanceof Collection<?> col && !col.contains(actual);
            default -> false;
        };
    }

    @SuppressWarnings("unchecked")
    private int compareValues(Object actual, Object expected) {
        if (actual instanceof BigDecimal a && expected instanceof BigDecimal b) {
            return a.compareTo(b);
        }
        if (actual instanceof Number a && expected instanceof Number b) {
            return BigDecimal.valueOf(a.doubleValue()).compareTo(BigDecimal.valueOf(b.doubleValue()));
        }
        if (actual instanceof Comparable a && expected instanceof Comparable) {
            return ((Comparable<Object>) a).compareTo(expected);
        }
        return 0;
    }
}
