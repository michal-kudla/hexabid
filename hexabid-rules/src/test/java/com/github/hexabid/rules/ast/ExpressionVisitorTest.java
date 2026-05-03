package com.github.hexabid.rules.ast;

import com.github.hexabid.rules.algebra.BooleanAlgebra;
import com.github.hexabid.rules.model.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionVisitorTest {

    @Nested
    @DisplayName("BooleanAlgebra evaluation")
    class BooleanAlgebraEvaluation {

        private final RuleContext context = RuleContext.builder()
            .productType("REAL_ESTATE")
            .estimatedValue(new BigDecimal("50000"))
            .bidderAge(25)
            .wadiumPaid(true)
            .build();

        @Test
        void constantTrue() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            assertThat(visitor.evaluate(new ConstantExpression(true))).isTrue();
        }

        @Test
        void constantFalse() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            assertThat(visitor.evaluate(new ConstantExpression(false))).isFalse();
        }

        @Test
        void attributeCheckEquals() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            assertThat(visitor.evaluate(
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE")
            )).isTrue();
        }

        @Test
        void attributeCheckNotEquals() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            assertThat(visitor.evaluate(
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.NOT_EQUALS, "ALCOHOL")
            )).isTrue();
        }

        @Test
        void metricComparisonGreaterThan() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            assertThat(visitor.evaluate(
                new MetricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.GREATER_THAN, new BigDecimal("10000"))
            )).isTrue();
        }

        @Test
        void metricComparisonLessThan() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            assertThat(visitor.evaluate(
                new MetricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.LESS_THAN, new BigDecimal("10000"))
            )).isFalse();
        }

        @Test
        void andExpression() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            var expr = new AndExpression(
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE"),
                new MetricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.GREATER_THAN, new BigDecimal("10000"))
            );
            assertThat(visitor.evaluate(expr)).isTrue();
        }

        @Test
        void andExpressionFailsWhenOneFalse() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            var expr = new AndExpression(
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "ALCOHOL"),
                new MetricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.GREATER_THAN, new BigDecimal("10000"))
            );
            assertThat(visitor.evaluate(expr)).isFalse();
        }

        @Test
        void orExpression() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            var expr = new OrExpression(
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "ALCOHOL"),
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE")
            );
            assertThat(visitor.evaluate(expr)).isTrue();
        }

        @Test
        void notExpression() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            var expr = new NotExpression(
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "ALCOHOL")
            );
            assertThat(visitor.evaluate(expr)).isTrue();
        }

        @Test
        void ifThenElseWhenConditionTrue() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            var expr = new IfThenElse(
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE"),
                new AttributeCheck(AttributeKey.WADIUM_PAID, AttributeCheck.Comparator.EQUALS, true),
                new ConstantExpression(false)
            );
            assertThat(visitor.evaluate(expr)).isTrue();
        }

        @Test
        void ifThenElseWhenConditionFalse() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            var expr = new IfThenElse(
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "ALCOHOL"),
                new ConstantExpression(true),
                new AttributeCheck(AttributeKey.WADIUM_PAID, AttributeCheck.Comparator.EQUALS, true)
            );
            assertThat(visitor.evaluate(expr)).isTrue();
        }

        @Test
        void attributeCheckIsNull() {
            var contextNoAge = RuleContext.builder().productType("ALCOHOL").build();
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(contextNoAge));
            assertThat(visitor.evaluate(
                new AttributeCheck(AttributeKey.BIDDER_AGE, AttributeCheck.Comparator.IS_NULL, null)
            )).isTrue();
        }

        @Test
        void attributeCheckIsNotNull() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            assertThat(visitor.evaluate(
                new AttributeCheck(AttributeKey.BIDDER_AGE, AttributeCheck.Comparator.IS_NOT_NULL, null)
            )).isTrue();
        }

        @Test
        void metricComparisonGreaterThanOrEqual() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            assertThat(visitor.evaluate(
                new MetricComparison(AttributeKey.BIDDER_AGE, AttributeCheck.Comparator.GREATER_THAN_OR_EQUAL, 18)
            )).isTrue();
        }

        @Test
        void complexNestedExpression() {
            var visitor = new ExpressionVisitor<>(new BooleanAlgebra(context));
            var expr = new AndExpression(
                new OrExpression(
                    new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "ALCOHOL"),
                    new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "TOBACCO")
                ),
                new NotExpression(
                    new MetricComparison(AttributeKey.BIDDER_AGE, AttributeCheck.Comparator.GREATER_THAN_OR_EQUAL, 18)
                )
            );
            assertThat(visitor.evaluate(expr)).isFalse();
        }
    }
}
