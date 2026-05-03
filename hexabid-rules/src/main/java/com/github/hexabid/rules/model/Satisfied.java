package com.github.hexabid.rules.model;

public record Satisfied(RuleName ruleName, RuleSeverity severity) implements RuleEvaluationResult {

    public Satisfied {
        if (ruleName == null || severity == null) {
            throw new IllegalArgumentException("ruleName and severity must not be null");
        }
    }
}
