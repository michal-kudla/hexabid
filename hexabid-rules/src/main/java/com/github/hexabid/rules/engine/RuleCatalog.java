package com.github.hexabid.rules.engine;

import com.github.hexabid.rules.algebra.BooleanAlgebra;
import com.github.hexabid.rules.ast.ExpressionVisitor;
import com.github.hexabid.rules.model.RuleContext;
import com.github.hexabid.rules.model.RulePhase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RuleCatalog {

    private final List<RuleDefinition> rules = new CopyOnWriteArrayList<>();

    public void register(RuleDefinition rule) {
        rules.add(rule);
    }

    public List<RuleDefinition> rulesForPhase(RulePhase phase) {
        return rules.stream()
            .filter(r -> r.phase() == phase)
            .toList();
    }

    public List<RuleDefinition> applicableRules(RulePhase phase, RuleContext context) {
        var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
        return rules.stream()
            .filter(r -> r.phase() == phase)
            .filter(r -> Boolean.TRUE.equals(visitor.evaluate(r.applicabilityCondition())))
            .toList();
    }

    public List<RuleDefinition> allRules() {
        return List.copyOf(rules);
    }
}
