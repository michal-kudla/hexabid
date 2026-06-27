package com.github.hexabid.rules.algebra;

import com.github.hexabid.rules.model.RuleEvaluationResult;

import java.util.List;

public record ExplainedResult(
    RuleEvaluationResult result,
    String description,
    List<Contribution> contributions
) {

    public ExplainedResult {
        if (result == null || description == null || contributions == null) {
            throw new IllegalArgumentException("result, description and contributions must not be null");
        }
    }

    public record Contribution(String source, String description, RuleEvaluationResult result) {

        public Contribution {
            if (source == null || description == null || result == null) {
                throw new IllegalArgumentException("source, description and result must not be null");
            }
        }
    }
}
