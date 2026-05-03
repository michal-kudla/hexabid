package com.github.hexabid.rules.ast;

import com.github.hexabid.rules.algebra.RuleAlgebra;

public final class ExpressionVisitor<R> {

    private final RuleAlgebra<R> algebra;

    public ExpressionVisitor(RuleAlgebra<R> algebra) {
        this.algebra = java.util.Objects.requireNonNull(algebra);
    }

    public R evaluate(Expression expr) {
        return switch (expr) {
            case AttributeCheck ac -> algebra.attributeCheck(ac.key(), ac.comparator(), ac.value());
            case AndExpression and -> algebra.and(evaluate(and.left()), evaluate(and.right()));
            case OrExpression or -> algebra.or(evaluate(or.left()), evaluate(or.right()));
            case NotExpression not -> algebra.not(evaluate(not.inner()));
            case IfThenElse ite -> algebra.ifThenElse(
                evaluate(ite.condition()),
                evaluate(ite.thenExpr()),
                evaluate(ite.elseExpr()));
            case ConstantExpression ce -> algebra.constant(ce.value());
            case MetricComparison mc -> algebra.metricComparison(mc.metric(), mc.comparator(), mc.threshold());
        };
    }
}
