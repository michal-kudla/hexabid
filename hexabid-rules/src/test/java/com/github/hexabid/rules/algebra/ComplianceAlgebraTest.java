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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ComplianceAlgebraTest {

    private static final RuleName RULE = RuleName.of("TEST_RULE");
    private static final String MSG = "Rule violated";

    private RuleContext context;

    @BeforeEach
    void setUp() {
        context = RuleContext.builder()
            .productType("REAL_ESTATE")
            .estimatedValue(new BigDecimal("50000"))
            .bidderAge(25)
            .wadiumPaid(true)
            .excisable(false)
            .build();
    }

    private ComplianceAlgebra blocking() {
        return ComplianceAlgebra.forRule(context, RULE, RuleSeverity.BLOCKING, MSG);
    }

    private ComplianceAlgebra warning() {
        return ComplianceAlgebra.forRule(context, RULE, RuleSeverity.WARNING, MSG);
    }

    private Satisfied satisfied() {
        return new Satisfied(RULE, RuleSeverity.BLOCKING);
    }

    private Satisfied satisfiedWarning() {
        return new Satisfied(RULE, RuleSeverity.WARNING);
    }

    private Violated violated() {
        return new Violated(RULE, RuleSeverity.BLOCKING, MSG);
    }

    private Violated violatedWarning() {
        return new Violated(RULE, RuleSeverity.WARNING, MSG);
    }

    private Pending pending() {
        return new Pending(RULE, RuleSeverity.BLOCKING, "Action required", null);
    }

    private Pending pendingWarning() {
        return new Pending(RULE, RuleSeverity.WARNING, "Action required", null);
    }

    @Nested
    @DisplayName("AND truth table")
    class AndTruthTable {

        @Test
        void andSatisfiedSatisfied() {
            var result = blocking().and(satisfied(), satisfied());
            assertThat(result).isInstanceOf(Satisfied.class);
        }

        @Test
        void andSatisfiedViolated() {
            var result = blocking().and(satisfied(), violated());
            assertThat(result).isInstanceOf(Violated.class);
        }

        @Test
        void andViolatedSatisfied() {
            var result = blocking().and(violated(), satisfied());
            assertThat(result).isInstanceOf(Violated.class);
        }

        @Test
        void andViolatedViolated() {
            var result = blocking().and(violated(), violated());
            assertThat(result).isInstanceOf(Violated.class);
        }

        @Test
        void andSatisfiedPending() {
            var result = blocking().and(satisfied(), pending());
            assertThat(result).isInstanceOf(Pending.class);
        }

        @Test
        void andPendingSatisfied() {
            var result = blocking().and(pending(), satisfied());
            assertThat(result).isInstanceOf(Pending.class);
        }

        @Test
        void andPendingViolated() {
            var result = blocking().and(pending(), violated());
            assertThat(result).isInstanceOf(Violated.class);
        }

        @Test
        void andViolatedPending() {
            var result = blocking().and(violated(), pending());
            assertThat(result).isInstanceOf(Violated.class);
        }

        @Test
        void andPendingPending() {
            var result = blocking().and(pending(), pending());
            assertThat(result).isInstanceOf(Pending.class);
        }
    }

    @Nested
    @DisplayName("OR truth table")
    class OrTruthTable {

        @Test
        void orSatisfiedSatisfied() {
            var result = blocking().or(satisfied(), satisfied());
            assertThat(result).isInstanceOf(Satisfied.class);
        }

        @Test
        void orSatisfiedViolated() {
            var result = blocking().or(satisfied(), violated());
            assertThat(result).isInstanceOf(Satisfied.class);
        }

        @Test
        void orViolatedSatisfied() {
            var result = blocking().or(violated(), satisfied());
            assertThat(result).isInstanceOf(Satisfied.class);
        }

        @Test
        void orViolatedViolated() {
            var result = blocking().or(violated(), violated());
            assertThat(result).isInstanceOf(Violated.class);
        }

        @Test
        void orSatisfiedPending() {
            var result = blocking().or(satisfied(), pending());
            assertThat(result).isInstanceOf(Satisfied.class);
        }

        @Test
        void orPendingSatisfied() {
            var result = blocking().or(pending(), satisfied());
            assertThat(result).isInstanceOf(Satisfied.class);
        }

        @Test
        void orViolatedPending() {
            var result = blocking().or(violated(), pending());
            assertThat(result).isInstanceOf(Pending.class);
        }

        @Test
        void orPendingViolated() {
            var result = blocking().or(pending(), violated());
            assertThat(result).isInstanceOf(Pending.class);
        }

        @Test
        void orPendingPending() {
            var result = blocking().or(pending(), pending());
            assertThat(result).isInstanceOf(Pending.class);
        }
    }

    @Nested
    @DisplayName("NOT truth table")
    class NotTruthTable {

        @Test
        void notSatisfied() {
            var result = blocking().not(satisfied());
            assertThat(result).isInstanceOf(Violated.class);
        }

        @Test
        void notViolated() {
            var result = blocking().not(violated());
            assertThat(result).isInstanceOf(Satisfied.class);
        }

        @Test
        void notPending() {
            var result = blocking().not(pending());
            assertThat(result).isInstanceOf(Pending.class);
        }
    }

    @Nested
    @DisplayName("IF-THEN-ELSE")
    class IfThenElseTruthTable {

        @Test
        void ifThenElseConditionSatisfiedReturnsThen() {
            var result = blocking().ifThenElse(satisfied(), satisfied(), violated());
            assertThat(result).isInstanceOf(Satisfied.class);
        }

        @Test
        void ifThenElseConditionViolatedReturnsElse() {
            var result = blocking().ifThenElse(violated(), satisfied(), violated());
            assertThat(result).isInstanceOf(Violated.class);
        }

        @Test
        void ifThenElseConditionPending() {
            var result = blocking().ifThenElse(pending(), satisfied(), violated());
            assertThat(result).isInstanceOf(Satisfied.class);
        }
    }

    @Nested
    @DisplayName("Constant values")
    class ConstantValues {

        @Test
        void constantTrue() {
            var result = blocking().constant(true);
            assertThat(result).isInstanceOf(Satisfied.class);
        }

        @Test
        void constantFalse() {
            var result = blocking().constant(false);
            assertThat(result).isInstanceOf(Violated.class);
        }
    }

    @Nested
    @DisplayName("Attribute check and metric comparison")
    class AttributeAndMetric {

        @Test
        void attributeCheckSatisfied() {
            var result = blocking().attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE");
            assertThat(result).isInstanceOf(Satisfied.class);
        }

        @Test
        void attributeCheckViolated() {
            var result = blocking().attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "ALCOHOL");
            assertThat(result).isInstanceOf(Violated.class);
        }

        @Test
        void metricComparisonSatisfied() {
            var result = blocking().metricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.GREATER_THAN, new BigDecimal("10000"));
            assertThat(result).isInstanceOf(Satisfied.class);
        }

        @Test
        void metricComparisonViolated() {
            var result = blocking().metricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.LESS_THAN, new BigDecimal("10000"));
            assertThat(result).isInstanceOf(Violated.class);
        }

        @Test
        void attributeCheckWithNullValue() {
            var partialCtx = RuleContext.builder().productType("ALCOHOL").build();
            var algebra = ComplianceAlgebra.forRule(partialCtx, RULE, RuleSeverity.BLOCKING, MSG);
            var result = algebra.attributeCheck(AttributeKey.WADIUM_PAID, AttributeCheck.Comparator.EQUALS, true);
            assertThat(result).isInstanceOf(Violated.class);
        }

        @Test
        void attributeCheckIsNullWhenAbsent() {
            var partialCtx = RuleContext.builder().productType("ALCOHOL").build();
            var algebra = ComplianceAlgebra.forRule(partialCtx, RULE, RuleSeverity.BLOCKING, MSG);
            var result = algebra.attributeCheck(AttributeKey.WADIUM_PAID, AttributeCheck.Comparator.IS_NULL, null);
            assertThat(result).isInstanceOf(Satisfied.class);
        }

        @Test
        void attributeCheckIsNotNullWhenPresent() {
            var result = blocking().attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.IS_NOT_NULL, null);
            assertThat(result).isInstanceOf(Satisfied.class);
        }
    }

    @Nested
    @DisplayName("Severity merging")
    class SeverityMerging {

        @Test
        void andMergesBlockingAndWarningTakesBlocking() {
            var result = blocking().and(satisfied(), satisfiedWarning());
            assertThat(result.severity()).isEqualTo(RuleSeverity.BLOCKING);
        }

        @Test
        void orMergesBlockingAndWarningTakesBlocking() {
            var result = blocking().or(violated(), violatedWarning());
            assertThat(result.severity()).isEqualTo(RuleSeverity.BLOCKING);
        }
    }
}
