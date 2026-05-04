package com.github.hexabid.rules.ast;

public sealed interface Expression
    permits AttributeCheck, AndExpression, OrExpression,
            NotExpression, IfThenElse, ConstantExpression,
            MetricComparison {
}
