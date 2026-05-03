package com.github.hexabid.rules.algebra;

import com.github.hexabid.rules.ast.AndExpression;
import com.github.hexabid.rules.ast.AttributeCheck;
import com.github.hexabid.rules.ast.AttributeKey;
import com.github.hexabid.rules.ast.ConstantExpression;
import com.github.hexabid.rules.ast.MetricComparison;
import com.github.hexabid.rules.ast.NotExpression;
import com.github.hexabid.rules.ast.OrExpression;
import com.github.hexabid.rules.model.RuleContext;
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

class ExplainedAlgebraTest {

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
            .build();
    }

    private ExplainedAlgebra algebra() {
        return ExplainedAlgebra.forRule(context, RULE, RuleSeverity.BLOCKING, MSG);
    }

    @Nested
    @DisplayName("Constant expressions")
    class Constants {

        @Test
        void constantTrueProducesSatisfied() {
            var result = algebra().constant(true);
            assertThat(result.result()).isInstanceOf(Satisfied.class);
            assertThat(result.description()).isEqualTo("always true");
            assertThat(result.contributions()).hasSize(1);
        }

        @Test
        void constantFalseProducesViolated() {
            var result = algebra().constant(false);
            assertThat(result.result()).isInstanceOf(Violated.class);
            assertThat(result.description()).isEqualTo("always false");
        }
    }

    @Nested
    @DisplayName("Attribute check explanations")
    class AttributeChecks {

        @Test
        void attributeCheckSatisfiedWithExplanation() {
            var result = algebra().attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE");
            assertThat(result.result()).isInstanceOf(Satisfied.class);
            assertThat(result.description()).contains("productType");
            assertThat(result.description()).contains("EQUALS");
            assertThat(result.description()).contains("REAL_ESTATE");
            assertThat(result.contributions()).hasSize(1);
            assertThat(result.contributions().getFirst().source()).isEqualTo("ATTRIBUTE_CHECK");
        }

        @Test
        void attributeCheckViolatedWithExplanation() {
            var result = algebra().attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "ALCOHOL");
            assertThat(result.result()).isInstanceOf(Violated.class);
            assertThat(result.description()).contains("productType");
            assertThat(result.description()).contains("actual=REAL_ESTATE");
        }

        @Test
        void metricComparisonWithExplanation() {
            var result = algebra().metricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.GREATER_THAN, new BigDecimal("10000"));
            assertThat(result.result()).isInstanceOf(Satisfied.class);
            assertThat(result.description()).contains("estimatedValue");
            assertThat(result.description()).contains("actual=50000");
            assertThat(result.contributions().getFirst().source()).isEqualTo("METRIC_COMPARISON");
        }
    }

    @Nested
    @DisplayName("Compound expressions with explanations")
    class CompoundExpressions {

        @Test
        void andExpressionWithContributions() {
            var left = algebra().attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE");
            var right = algebra().metricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.GREATER_THAN, new BigDecimal("10000"));
            var result = algebra().and(left, right);
            assertThat(result.result()).isInstanceOf(Satisfied.class);
            assertThat(result.description()).contains("AND");
            assertThat(result.contributions()).hasSize(2);
        }

        @Test
        void orExpressionWithContributions() {
            var left = algebra().attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "ALCOHOL");
            var right = algebra().attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE");
            var result = algebra().or(left, right);
            assertThat(result.result()).isInstanceOf(Satisfied.class);
            assertThat(result.description()).contains("OR");
        }

        @Test
        void notExpressionWithContribution() {
            var inner = algebra().attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "ALCOHOL");
            var result = algebra().not(inner);
            assertThat(result.result()).isInstanceOf(Satisfied.class);
            assertThat(result.description()).contains("NOT");
            assertThat(result.contributions()).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        void fullExpressionTreeExplanation() {
            var visitor = new com.github.hexabid.rules.ast.ExpressionVisitor<>(algebra());
            var expr = new AndExpression(
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE"),
                new NotExpression(
                    new MetricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.LESS_THAN, new BigDecimal("10000"))
                )
            );
            var result = visitor.evaluate(expr);
            assertThat(result.result()).isInstanceOf(Satisfied.class);
            assertThat(result.contributions()).hasSizeGreaterThanOrEqualTo(2);
            assertThat(result.description()).isNotBlank();
        }
    }
}
