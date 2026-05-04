package com.github.hexabid.rules.ast;

import java.util.Objects;

public record IfThenElse(Expression condition, Expression thenExpr, Expression elseExpr) implements Expression {

    public IfThenElse {
        Objects.requireNonNull(condition);
        Objects.requireNonNull(thenExpr);
        Objects.requireNonNull(elseExpr);
    }
}
