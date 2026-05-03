package com.github.hexabid.rules.model;

import org.jspecify.annotations.Nullable;

public sealed interface RuleEvaluationResult permits Satisfied, Pending, Violated {
    RuleName ruleName();

    RuleSeverity severity();
}
