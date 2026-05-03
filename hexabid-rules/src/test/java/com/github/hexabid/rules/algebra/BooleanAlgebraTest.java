package com.github.hexabid.rules.algebra;

import com.github.hexabid.rules.ast.AttributeCheck;
import com.github.hexabid.rules.ast.AttributeKey;
import com.github.hexabid.rules.model.RuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BooleanAlgebraTest {

    private BooleanAlgebra algebra;
    private RuleContext context;

    @BeforeEach
    void setUp() {
        context = RuleContext.builder()
            .productType("REAL_ESTATE")
            .estimatedValue(new BigDecimal("50000"))
            .bidderAge(25)
            .wadiumPaid(true)
            .excisable(false)
            .imported(true)
            .build();
        algebra = new BooleanAlgebra(context);
    }

    @Nested
    @DisplayName("Logical operations")
    class LogicalOperations {

        @Test
        void andTrueTrue() {
            assertThat(algebra.and(true, true)).isTrue();
        }

        @Test
        void andTrueFalse() {
            assertThat(algebra.and(true, false)).isFalse();
        }

        @Test
        void andFalseTrue() {
            assertThat(algebra.and(false, true)).isFalse();
        }

        @Test
        void orTrueFalse() {
            assertThat(algebra.or(true, false)).isTrue();
        }

        @Test
        void orFalseFalse() {
            assertThat(algebra.or(false, false)).isFalse();
        }

        @Test
        void notTrue() {
            assertThat(algebra.not(true)).isFalse();
        }

        @Test
        void notFalse() {
            assertThat(algebra.not(false)).isTrue();
        }

        @Test
        void ifThenElseConditionTrue() {
            assertThat(algebra.ifThenElse(true, true, false)).isTrue();
        }

        @Test
        void ifThenElseConditionFalse() {
            assertThat(algebra.ifThenElse(false, true, false)).isFalse();
        }

        @Test
        void constantTrue() {
            assertThat(algebra.constant(true)).isTrue();
        }

        @Test
        void constantFalse() {
            assertThat(algebra.constant(false)).isFalse();
        }
    }

    @Nested
    @DisplayName("Attribute checks")
    class AttributeChecks {

        @Test
        void attributeCheckEqualsTrue() {
            assertThat(algebra.attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE")).isTrue();
        }

        @Test
        void attributeCheckEqualsFalse() {
            assertThat(algebra.attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "ALCOHOL")).isFalse();
        }

        @Test
        void attributeCheckNotEqualsTrue() {
            assertThat(algebra.attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.NOT_EQUALS, "ALCOHOL")).isTrue();
        }

        @Test
        void attributeCheckIsNullWhenPresent() {
            assertThat(algebra.attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.IS_NULL, null)).isFalse();
        }

        @Test
        void attributeCheckIsNotNullWhenPresent() {
            assertThat(algebra.attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.IS_NOT_NULL, null)).isTrue();
        }

        @Test
        void attributeCheckIsNullWhenAbsent() {
            var emptyContext = RuleContext.builder().build();
            var emptyAlgebra = new BooleanAlgebra(emptyContext);
            assertThat(emptyAlgebra.attributeCheck(AttributeKey.BIDDER_AGE, AttributeCheck.Comparator.IS_NULL, null)).isTrue();
        }

        @Test
        void attributeCheckIsNotNullWhenAbsent() {
            var emptyContext = RuleContext.builder().build();
            var emptyAlgebra = new BooleanAlgebra(emptyContext);
            assertThat(emptyAlgebra.attributeCheck(AttributeKey.BIDDER_AGE, AttributeCheck.Comparator.IS_NOT_NULL, null)).isFalse();
        }

        @Test
        void attributeCheckEqualsWhenValueNull() {
            var partialContext = RuleContext.builder().productType("ALCOHOL").build();
            var partialAlgebra = new BooleanAlgebra(partialContext);
            assertThat(partialAlgebra.attributeCheck(AttributeKey.WADIUM_PAID, AttributeCheck.Comparator.EQUALS, true)).isFalse();
        }

        @Test
        void attributeCheckInTrue() {
            assertThat(algebra.attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.IN, List.of("REAL_ESTATE", "VEHICLE"))).isTrue();
        }

        @Test
        void attributeCheckInFalse() {
            assertThat(algebra.attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.IN, List.of("ALCOHOL", "TOBACCO"))).isFalse();
        }

        @Test
        void attributeCheckNotInTrue() {
            assertThat(algebra.attributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.NOT_IN, List.of("ALCOHOL", "TOBACCO"))).isTrue();
        }
    }

    @Nested
    @DisplayName("Metric comparisons")
    class MetricComparisons {

        @Test
        void metricGreaterThanTrue() {
            assertThat(algebra.metricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.GREATER_THAN, new BigDecimal("10000"))).isTrue();
        }

        @Test
        void metricGreaterThanFalse() {
            assertThat(algebra.metricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.GREATER_THAN, new BigDecimal("99999"))).isFalse();
        }

        @Test
        void metricGreaterThanOrEqualTrue() {
            assertThat(algebra.metricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.GREATER_THAN_OR_EQUAL, new BigDecimal("50000"))).isTrue();
        }

        @Test
        void metricLessThanTrue() {
            assertThat(algebra.metricComparison(AttributeKey.BIDDER_AGE, AttributeCheck.Comparator.LESS_THAN, 30)).isTrue();
        }

        @Test
        void metricLessThanOrEqualTrue() {
            assertThat(algebra.metricComparison(AttributeKey.BIDDER_AGE, AttributeCheck.Comparator.LESS_THAN_OR_EQUAL, 25)).isTrue();
        }

        @Test
        void metricGreaterThanOrEqualIntegerComparison() {
            assertThat(algebra.metricComparison(AttributeKey.BIDDER_AGE, AttributeCheck.Comparator.GREATER_THAN_OR_EQUAL, 18)).isTrue();
        }

        @Test
        void metricLessThanIntegerComparison() {
            assertThat(algebra.metricComparison(AttributeKey.BIDDER_AGE, AttributeCheck.Comparator.LESS_THAN, 18)).isFalse();
        }

        @Test
        void metricComparisonWhenValueNull() {
            var partialContext = RuleContext.builder().productType("ALCOHOL").build();
            var partialAlgebra = new BooleanAlgebra(partialContext);
            assertThat(partialAlgebra.metricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.GREATER_THAN, new BigDecimal("10000"))).isFalse();
        }
    }
}
