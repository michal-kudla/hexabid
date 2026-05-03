package com.github.hexabid.rules.algebra;

import com.github.hexabid.rules.ast.AttributeCheck;
import com.github.hexabid.rules.ast.AttributeKey;

public interface RuleAlgebra<R> {
    R and(R left, R right);

    R or(R left, R right);

    R not(R inner);

    R ifThenElse(R condition, R thenResult, R elseResult);

    R constant(boolean value);

    R attributeCheck(AttributeKey key, AttributeCheck.Comparator cmp, Object value);

    R metricComparison(AttributeKey metric, AttributeCheck.Comparator cmp, Object threshold);
}
