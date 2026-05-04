package com.github.hexabid.rules.model;

import org.jspecify.annotations.Nullable;

public record Pending(RuleName ruleName, RuleSeverity severity, String requiredAction,
                      @Nullable String deadline) implements RuleEvaluationResult {

    public Pending {
        if (ruleName == null || severity == null || requiredAction == null) {
            throw new IllegalArgumentException("ruleName, severity and requiredAction must not be null");
        }
    }
}
