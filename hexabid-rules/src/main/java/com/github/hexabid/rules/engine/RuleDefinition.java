package com.github.hexabid.rules.engine;

import com.github.hexabid.rules.ast.Expression;
import com.github.hexabid.rules.model.RuleId;
import com.github.hexabid.rules.model.RuleName;
import com.github.hexabid.rules.model.RulePhase;
import com.github.hexabid.rules.model.RuleSeverity;

import java.util.Objects;

public final class RuleDefinition {

    private final RuleId id;
    private final RuleName name;
    private final RulePhase phase;
    private final RuleSeverity severity;
    private final Expression applicabilityCondition;
    private final Expression ruleCondition;
    private final String violationMessage;

    private RuleDefinition(RuleId id, RuleName name, RulePhase phase, RuleSeverity severity,
                           Expression applicabilityCondition, Expression ruleCondition,
                           String violationMessage) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.phase = Objects.requireNonNull(phase);
        this.severity = Objects.requireNonNull(severity);
        this.applicabilityCondition = Objects.requireNonNull(applicabilityCondition);
        this.ruleCondition = Objects.requireNonNull(ruleCondition);
        this.violationMessage = Objects.requireNonNull(violationMessage);
    }

    public static RuleDefinition of(RuleName name, RulePhase phase, RuleSeverity severity,
                                    Expression applicability, Expression condition, String message) {
        return new RuleDefinition(RuleId.newId(), name, phase, severity, applicability, condition, message);
    }

    public RuleId id() { return id; }
    public RuleName name() { return name; }
    public RulePhase phase() { return phase; }
    public RuleSeverity severity() { return severity; }
    public Expression applicabilityCondition() { return applicabilityCondition; }
    public Expression ruleCondition() { return ruleCondition; }
    public String violationMessage() { return violationMessage; }
}
