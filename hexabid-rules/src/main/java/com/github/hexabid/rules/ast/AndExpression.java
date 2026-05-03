package com.github.hexabid.rules.ast;

import java.util.Objects;

public record AndExpression(Expression left, Expression right) implements Expression {

    public AndExpression {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
    }
}
