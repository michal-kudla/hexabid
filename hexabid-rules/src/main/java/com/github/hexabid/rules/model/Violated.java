package com.github.hexabid.rules.model;

public record Violated(RuleName ruleName, RuleSeverity severity, String violationMessage) implements RuleEvaluationResult {

    public Violated {
        if (ruleName == null || severity == null || violationMessage == null) {
            throw new IllegalArgumentException("ruleName, severity and violationMessage must not be null");
        }
    }
}
