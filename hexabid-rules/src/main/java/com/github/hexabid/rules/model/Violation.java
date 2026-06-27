package com.github.hexabid.rules.model;

public record Violation(RuleName ruleName, String message, RuleSeverity severity) {

    public Violation {
        if (ruleName == null || message == null || severity == null) {
            throw new IllegalArgumentException("ruleName, message and severity must not be null");
        }
    }

    public static Violation from(Violated violated) {
        return new Violation(violated.ruleName(), violated.violationMessage(), violated.severity());
    }
}
