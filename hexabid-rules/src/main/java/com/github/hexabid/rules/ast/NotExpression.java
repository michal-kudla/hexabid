package com.github.hexabid.rules.ast;

import java.util.Objects;

public record NotExpression(Expression inner) implements Expression {

    public NotExpression {
        Objects.requireNonNull(inner);
    }
}
