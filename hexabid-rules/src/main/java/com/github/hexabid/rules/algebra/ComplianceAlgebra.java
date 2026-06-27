package com.github.hexabid.rules.algebra;

import com.github.hexabid.rules.ast.AttributeCheck;
import com.github.hexabid.rules.ast.AttributeKey;
import com.github.hexabid.rules.model.Pending;
import com.github.hexabid.rules.model.RuleEvaluationResult;
import com.github.hexabid.rules.model.RuleName;
import com.github.hexabid.rules.model.RuleSeverity;
import com.github.hexabid.rules.model.Satisfied;
import com.github.hexabid.rules.model.Violated;
import com.github.hexabid.rules.model.RuleContext;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;

public final class ComplianceAlgebra implements RuleAlgebra<RuleEvaluationResult> {

    private final RuleContext context;
    private final RuleName currentRuleName;
    private final RuleSeverity currentSeverity;
    private final String violationMessage;

    private ComplianceAlgebra(RuleContext context, RuleName ruleName, RuleSeverity severity, String violationMessage) {
        this.context = Objects.requireNonNull(context);
        this.currentRuleName = ruleName;
        this.currentSeverity = severity;
        this.violationMessage = violationMessage;
    }

    public static ComplianceAlgebra forRule(RuleContext context, RuleName ruleName, RuleSeverity severity, String violationMessage) {
        return new ComplianceAlgebra(context, ruleName, severity, violationMessage);
    }

    @Override
    public RuleEvaluationResult and(RuleEvaluationResult left, RuleEvaluationResult right) {
        var merged = mergeSeverity(left.severity(), right.severity());
        if (left instanceof Violated || right instanceof Violated) {
            return new Violated(currentRuleName, merged, violationMessage);
        }
        if (left instanceof Pending || right instanceof Pending) {
            var pendingAction = extractPendingAction(left, right);
            return new Pending(currentRuleName, merged, pendingAction, null);
        }
        return new Satisfied(currentRuleName, merged);
    }

    @Override
    public RuleEvaluationResult or(RuleEvaluationResult left, RuleEvaluationResult right) {
        var merged = mergeSeverity(left.severity(), right.severity());
        if (left instanceof Satisfied || right instanceof Satisfied) {
            return new Satisfied(currentRuleName, merged);
        }
        if (left instanceof Pending || right instanceof Pending) {
            var pendingAction = extractPendingAction(left, right);
            return new Pending(currentRuleName, merged, pendingAction, null);
        }
        return new Violated(currentRuleName, merged, violationMessage);
    }

    @Override
    public RuleEvaluationResult not(RuleEvaluationResult inner) {
        return switch (inner) {
            case Satisfied s -> new Violated(s.ruleName(), s.severity(), violationMessage);
            case Violated v -> new Satisfied(v.ruleName(), v.severity());
            case Pending p -> new Pending(p.ruleName(), p.severity(), p.requiredAction(), p.deadline());
        };
    }

    @Override
    public RuleEvaluationResult ifThenElse(RuleEvaluationResult condition, RuleEvaluationResult thenResult, RuleEvaluationResult elseResult) {
        return switch (condition) {
            case Satisfied s -> thenResult;
            case Violated v -> elseResult;
            case Pending p -> thenResult.severity().ordinal() <= elseResult.severity().ordinal() ? thenResult : elseResult;
        };
    }

    @Override
    public RuleEvaluationResult constant(boolean value) {
        return value
            ? new Satisfied(currentRuleName, currentSeverity)
            : new Violated(currentRuleName, currentSeverity, violationMessage);
    }

    @Override
    public RuleEvaluationResult attributeCheck(AttributeKey key, AttributeCheck.Comparator cmp, Object value) {
        var actual = context.get(key);
        var satisfied = compare(actual, cmp, value);
        return satisfied
            ? new Satisfied(currentRuleName, currentSeverity)
            : new Violated(currentRuleName, currentSeverity, violationMessage);
    }

    @Override
    public RuleEvaluationResult metricComparison(AttributeKey metric, AttributeCheck.Comparator cmp, Object threshold) {
        var actual = context.get(metric);
        var satisfied = compare(actual, cmp, threshold);
        return satisfied
            ? new Satisfied(currentRuleName, currentSeverity)
            : new Violated(currentRuleName, currentSeverity, violationMessage);
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

    private RuleSeverity mergeSeverity(RuleSeverity a, RuleSeverity b) {
        return a.ordinal() < b.ordinal() ? a : b;
    }

    private String extractPendingAction(RuleEvaluationResult left, RuleEvaluationResult right) {
        var leftAction = left instanceof Pending p ? p.requiredAction() : "";
        var rightAction = right instanceof Pending p ? p.requiredAction() : "";
        return leftAction.isEmpty() ? rightAction : leftAction;
    }
}
