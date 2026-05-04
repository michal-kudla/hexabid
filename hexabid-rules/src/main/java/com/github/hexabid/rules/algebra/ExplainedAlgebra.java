package com.github.hexabid.rules.algebra;

import com.github.hexabid.rules.ast.AttributeCheck;
import com.github.hexabid.rules.ast.AttributeKey;
import com.github.hexabid.rules.model.Pending;
import com.github.hexabid.rules.model.RuleContext;
import com.github.hexabid.rules.model.RuleEvaluationResult;
import com.github.hexabid.rules.model.RuleName;
import com.github.hexabid.rules.model.RuleSeverity;
import com.github.hexabid.rules.model.Satisfied;
import com.github.hexabid.rules.model.Violated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class ExplainedAlgebra implements RuleAlgebra<ExplainedResult> {

    private final RuleContext context;
    private final RuleName currentRuleName;
    private final RuleSeverity currentSeverity;
    private final String violationMessage;

    private ExplainedAlgebra(RuleContext context, RuleName ruleName, RuleSeverity severity, String violationMessage) {
        this.context = Objects.requireNonNull(context);
        this.currentRuleName = ruleName;
        this.currentSeverity = severity;
        this.violationMessage = violationMessage;
    }

    public static ExplainedAlgebra forRule(RuleContext context, RuleName ruleName, RuleSeverity severity, String violationMessage) {
        return new ExplainedAlgebra(context, ruleName, severity, violationMessage);
    }

    @Override
    public ExplainedResult and(ExplainedResult left, ExplainedResult right) {
        var merged = mergeSeverity(left.result().severity(), right.result().severity());
        var contributions = mergeContributions(left, right);

        if (left.result() instanceof Violated || right.result() instanceof Violated) {
            var result = new Violated(currentRuleName, merged, violationMessage);
            var desc = left.description() + " AND " + right.description();
            return new ExplainedResult(result, desc, contributions);
        }
        if (left.result() instanceof Pending || right.result() instanceof Pending) {
            var pendingAction = extractPendingAction(left.result(), right.result());
            var result = new Pending(currentRuleName, merged, pendingAction, null);
            var desc = left.description() + " AND " + right.description();
            return new ExplainedResult(result, desc, contributions);
        }
        var result = new Satisfied(currentRuleName, merged);
        var desc = left.description() + " AND " + right.description();
        return new ExplainedResult(result, desc, contributions);
    }

    @Override
    public ExplainedResult or(ExplainedResult left, ExplainedResult right) {
        var merged = mergeSeverity(left.result().severity(), right.result().severity());
        var contributions = mergeContributions(left, right);

        if (left.result() instanceof Satisfied || right.result() instanceof Satisfied) {
            var result = new Satisfied(currentRuleName, merged);
            var desc = left.description() + " OR " + right.description();
            return new ExplainedResult(result, desc, contributions);
        }
        if (left.result() instanceof Pending || right.result() instanceof Pending) {
            var pendingAction = extractPendingAction(left.result(), right.result());
            var result = new Pending(currentRuleName, merged, pendingAction, null);
            var desc = left.description() + " OR " + right.description();
            return new ExplainedResult(result, desc, contributions);
        }
        var result = new Violated(currentRuleName, merged, violationMessage);
        var desc = left.description() + " OR " + right.description();
        return new ExplainedResult(result, desc, contributions);
    }

    @Override
    public ExplainedResult not(ExplainedResult inner) {
        var inverted = switch (inner.result()) {
            case Satisfied s -> (RuleEvaluationResult) new Violated(s.ruleName(), s.severity(), violationMessage);
            case Violated v -> new Satisfied(v.ruleName(), v.severity());
            case Pending p -> new Pending(p.ruleName(), p.severity(), p.requiredAction(), p.deadline());
        };
        var desc = "NOT(" + inner.description() + ")";
        var contribution = new ExplainedResult.Contribution("NOT", desc, inverted);
        var contributions = new ArrayList<>(inner.contributions());
        contributions.add(contribution);
        return new ExplainedResult(inverted, desc, contributions);
    }

    @Override
    public ExplainedResult ifThenElse(ExplainedResult condition, ExplainedResult thenResult, ExplainedResult elseResult) {
        var chosen = switch (condition.result()) {
            case Satisfied s -> thenResult;
            case Violated v -> elseResult;
            case Pending p -> thenResult.result().severity().ordinal() <= elseResult.result().severity().ordinal() ? thenResult : elseResult;
        };
        var desc = "IF " + condition.description() + " THEN " + thenResult.description() + " ELSE " + elseResult.description();
        var contributions = new ArrayList<>(condition.contributions());
        contributions.addAll(chosen.contributions());
        contributions.add(new ExplainedResult.Contribution("IF-THEN-ELSE", desc, chosen.result()));
        return new ExplainedResult(chosen.result(), desc, contributions);
    }

    @Override
    public ExplainedResult constant(boolean value) {
        var result = value
            ? (RuleEvaluationResult) new Satisfied(currentRuleName, currentSeverity)
            : new Violated(currentRuleName, currentSeverity, violationMessage);
        var desc = value ? "always true" : "always false";
        var contribution = new ExplainedResult.Contribution("CONSTANT", desc, result);
        return new ExplainedResult(result, desc, List.of(contribution));
    }

    @Override
    public ExplainedResult attributeCheck(AttributeKey key, AttributeCheck.Comparator cmp, Object value) {
        var actual = context.get(key);
        var satisfied = compare(actual, cmp, value);
        var result = satisfied
            ? (RuleEvaluationResult) new Satisfied(currentRuleName, currentSeverity)
            : new Violated(currentRuleName, currentSeverity, violationMessage);
        var desc = key.name() + " " + cmp + " " + value + " (actual=" + actual + ")";
        var contribution = new ExplainedResult.Contribution("ATTRIBUTE_CHECK", desc, result);
        return new ExplainedResult(result, desc, List.of(contribution));
    }

    @Override
    public ExplainedResult metricComparison(AttributeKey metric, AttributeCheck.Comparator cmp, Object threshold) {
        var actual = context.get(metric);
        var satisfied = compare(actual, cmp, threshold);
        var result = satisfied
            ? (RuleEvaluationResult) new Satisfied(currentRuleName, currentSeverity)
            : new Violated(currentRuleName, currentSeverity, violationMessage);
        var desc = metric.name() + " " + cmp + " " + threshold + " (actual=" + actual + ")";
        var contribution = new ExplainedResult.Contribution("METRIC_COMPARISON", desc, result);
        return new ExplainedResult(result, desc, List.of(contribution));
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

    private List<ExplainedResult.Contribution> mergeContributions(ExplainedResult left, ExplainedResult right) {
        var merged = new ArrayList<>(left.contributions());
        merged.addAll(right.contributions());
        return merged;
    }
}
