package com.github.hexabid.rules.ast;

import java.util.Objects;

public record OrExpression(Expression left, Expression right) implements Expression {

    public OrExpression {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
    }
}
