package com.github.hexabid.rules.engine;

import com.github.hexabid.rules.algebra.BooleanAlgebra;
import com.github.hexabid.rules.algebra.ComplianceAlgebra;
import com.github.hexabid.rules.algebra.ExplainedAlgebra;
import com.github.hexabid.rules.algebra.ExplainedResult;
import com.github.hexabid.rules.ast.ExpressionVisitor;
import com.github.hexabid.rules.model.Pending;
import com.github.hexabid.rules.model.RuleContext;
import com.github.hexabid.rules.model.RuleEvaluationResult;
import com.github.hexabid.rules.model.RulePhase;
import com.github.hexabid.rules.model.RuleSeverity;
import com.github.hexabid.rules.model.Satisfied;
import com.github.hexabid.rules.model.Violated;
import com.github.hexabid.rules.model.Violation;

import java.util.ArrayList;
import java.util.List;

public final class RuleEngine {

    private final RuleCatalog catalog;

    public RuleEngine(RuleCatalog catalog) {
        this.catalog = catalog;
    }

    public List<RuleEvaluationResult> evaluateAll(RulePhase phase, RuleContext context) {
        var applicableRules = catalog.applicableRules(phase, context);
        var results = new ArrayList<RuleEvaluationResult>();

        for (var rule : applicableRules) {
            var algebra = ComplianceAlgebra.forRule(context, rule.name(), rule.severity(), rule.violationMessage());
            var visitor = new ExpressionVisitor<>(algebra);
            var result = visitor.evaluate(rule.ruleCondition());
            results.add(result);
        }
        return results;
    }

    public boolean hasBlockingViolations(RulePhase phase, RuleContext context) {
        return evaluateAll(phase, context).stream()
            .anyMatch(r -> r instanceof Violated v && v.severity() == RuleSeverity.BLOCKING);
    }

    public List<Violated> violations(RulePhase phase, RuleContext context) {
        return evaluateAll(phase, context).stream()
            .filter(Violated.class::isInstance)
            .map(Violated.class::cast)
            .toList();
    }

    public List<Pending> pendingItems(RulePhase phase, RuleContext context) {
        return evaluateAll(phase, context).stream()
            .filter(Pending.class::isInstance)
            .map(Pending.class::cast)
            .toList();
    }

    public List<Satisfied> satisfiedRules(RulePhase phase, RuleContext context) {
        return evaluateAll(phase, context).stream()
            .filter(Satisfied.class::isInstance)
            .map(Satisfied.class::cast)
            .toList();
    }

    public List<ExplainedResult> evaluateWithExplanation(RulePhase phase, RuleContext context) {
        var applicableRules = catalog.applicableRules(phase, context);
        var explanations = new ArrayList<ExplainedResult>();

        for (var rule : applicableRules) {
            var algebra = ExplainedAlgebra.forRule(context, rule.name(), rule.severity(), rule.violationMessage());
            var visitor = new ExpressionVisitor<>(algebra);
            var result = visitor.evaluate(rule.ruleCondition());
            explanations.add(result);
        }
        return explanations;
    }

    public List<Violation> violationDetails(RulePhase phase, RuleContext context) {
        return violations(phase, context).stream()
            .map(Violation::from)
            .toList();
    }
}
