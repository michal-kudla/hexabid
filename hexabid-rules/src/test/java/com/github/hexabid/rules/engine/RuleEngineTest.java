package com.github.hexabid.rules.engine;

import com.github.hexabid.rules.ast.AttributeCheck;
import com.github.hexabid.rules.ast.ConstantExpression;
import com.github.hexabid.rules.ast.AndExpression;
import com.github.hexabid.rules.ast.AttributeKey;
import com.github.hexabid.rules.ast.MetricComparison;
import com.github.hexabid.rules.ast.NotExpression;
import com.github.hexabid.rules.ast.OrExpression;
import com.github.hexabid.rules.model.Pending;
import com.github.hexabid.rules.model.RuleContext;
import com.github.hexabid.rules.model.RuleName;
import com.github.hexabid.rules.model.RulePhase;
import com.github.hexabid.rules.model.RuleSeverity;
import com.github.hexabid.rules.model.Satisfied;
import com.github.hexabid.rules.model.Violated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEngineTest {

    private RuleCatalog catalog;
    private RuleEngine engine;

    @BeforeEach
    void setUp() {
        catalog = new RuleCatalog();
        engine = new RuleEngine(catalog);
    }

    @Nested
    @DisplayName("Participation rules")
    class ParticipationRules {

        @BeforeEach
        void registerParticipationRules() {
            catalog.register(RuleDefinition.of(
                RuleName.of("WADIUM_10_PERCENT_ABOVE_10K"),
                RulePhase.PARTICIPATION,
                RuleSeverity.BLOCKING,
                new AndExpression(
                    new MetricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.GREATER_THAN, new BigDecimal("10000")),
                    new NotExpression(new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE"))
                ),
                new AttributeCheck(AttributeKey.WADIUM_PAID, AttributeCheck.Comparator.EQUALS, true),
                "Wadium 10% is required for products above 10000 PLN"
            ));

            catalog.register(RuleDefinition.of(
                RuleName.of("AGE_VERIFICATION_ALCOHOL_TOBACCO"),
                RulePhase.PARTICIPATION,
                RuleSeverity.BLOCKING,
                new OrExpression(
                    new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "ALCOHOL"),
                    new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "TOBACCO")
                ),
                new MetricComparison(AttributeKey.BIDDER_AGE, AttributeCheck.Comparator.GREATER_THAN_OR_EQUAL, 18),
                "Age verification required for alcohol or tobacco"
            ));

            catalog.register(RuleDefinition.of(
                RuleName.of("VIEWING_DATE_REAL_ESTATE"),
                RulePhase.PARTICIPATION,
                RuleSeverity.BLOCKING,
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE"),
                new AttributeCheck(AttributeKey.VIEWING_DATE_SCHEDULED, AttributeCheck.Comparator.EQUALS, true),
                "Viewing date required for real estate"
            ));
        }

        @Test
        void realEstateWithViewingDateAndWadium_satisfied() {
            var context = RuleContext.builder()
                .productType("REAL_ESTATE")
                .estimatedValue(new BigDecimal("50000"))
                .wadiumPaid(true)
                .viewingDateScheduled(true)
                .build();

            var results = engine.evaluateAll(RulePhase.PARTICIPATION, context);
            assertThat(results).hasSize(1);
            assertThat(results.getFirst()).isInstanceOf(Satisfied.class);
            assertThat(engine.hasBlockingViolations(RulePhase.PARTICIPATION, context)).isFalse();
        }

        @Test
        void realEstateWithoutViewingDate_violated() {
            var context = RuleContext.builder()
                .productType("REAL_ESTATE")
                .estimatedValue(new BigDecimal("50000"))
                .wadiumPaid(true)
                .viewingDateScheduled(false)
                .build();

            var results = engine.evaluateAll(RulePhase.PARTICIPATION, context);
            assertThat(results).hasSize(1);
            assertThat(results.getFirst()).isInstanceOf(Violated.class);
            assertThat(engine.hasBlockingViolations(RulePhase.PARTICIPATION, context)).isTrue();
        }

        @Test
        void alcoholUnderage_violated() {
            var context = RuleContext.builder()
                .productType("ALCOHOL")
                .estimatedValue(new BigDecimal("500"))
                .bidderAge(16)
                .wadiumPaid(false)
                .build();

            var results = engine.evaluateAll(RulePhase.PARTICIPATION, context);
            assertThat(results.stream().filter(Violated.class::isInstance).count()).isGreaterThanOrEqualTo(1);
            assertThat(engine.hasBlockingViolations(RulePhase.PARTICIPATION, context)).isTrue();
        }

        @Test
        void alcoholAdult_satisfied() {
            var context = RuleContext.builder()
                .productType("ALCOHOL")
                .estimatedValue(new BigDecimal("500"))
                .bidderAge(25)
                .build();

            var results = engine.evaluateAll(RulePhase.PARTICIPATION, context);
            assertThat(results).hasSize(1);
            assertThat(results.getFirst()).isInstanceOf(Satisfied.class);
        }

        @Test
        void generalProductAbove10kWithoutWadium_violated() {
            var context = RuleContext.builder()
                .productType("VEHICLE")
                .estimatedValue(new BigDecimal("50000"))
                .wadiumPaid(false)
                .build();

            var results = engine.evaluateAll(RulePhase.PARTICIPATION, context);
            assertThat(results.getFirst()).isInstanceOf(Violated.class);
            assertThat(engine.hasBlockingViolations(RulePhase.PARTICIPATION, context)).isTrue();
        }

        @Test
        void generalProductBelow10k_noWadiumRuleApplicable() {
            var context = RuleContext.builder()
                .productType("VEHICLE")
                .estimatedValue(new BigDecimal("5000"))
                .wadiumPaid(false)
                .build();

            var results = engine.evaluateAll(RulePhase.PARTICIPATION, context);
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Settlement rules")
    class SettlementRules {

        @BeforeEach
        void registerSettlementRules() {
            catalog.register(RuleDefinition.of(
                RuleName.of("FULL_PAYMENT_SETTLEMENT"),
                RulePhase.SETTLEMENT,
                RuleSeverity.BLOCKING,
                new ConstantExpression(true),
                new AttributeCheck(AttributeKey.FULL_PAYMENT_RECEIVED, AttributeCheck.Comparator.EQUALS, true),
                "Full payment required for settlement"
            ));

            catalog.register(RuleDefinition.of(
                RuleName.of("EXCISE_ORIGINAL_SETTLEMENT"),
                RulePhase.SETTLEMENT,
                RuleSeverity.BLOCKING,
                new AttributeCheck(AttributeKey.EXCISABLE, AttributeCheck.Comparator.EQUALS, true),
                new AttributeCheck(AttributeKey.EXCISE_DOCUMENT_STATUS, AttributeCheck.Comparator.EQUALS, "ORIGINAL"),
                "Original excise document required for settlement"
            ));

            catalog.register(RuleDefinition.of(
                RuleName.of("REAL_ESTATE_DOCUMENT_3_DAYS"),
                RulePhase.SETTLEMENT,
                RuleSeverity.BLOCKING,
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE"),
                new AndExpression(
                    new AttributeCheck(AttributeKey.SETTLEMENT_DOCUMENT_PROVIDED, AttributeCheck.Comparator.EQUALS, true),
                    new MetricComparison(AttributeKey.DAYS_SINCE_CLOSE, AttributeCheck.Comparator.LESS_THAN_OR_EQUAL, 3)
                ),
                "Real estate settlement document required within 3 days"
            ));
        }

        @Test
        void realEstateFullSettlement_satisfied() {
            var context = RuleContext.builder()
                .productType("REAL_ESTATE")
                .fullPaymentReceived(true)
                .settlementDocumentProvided(true)
                .daysSinceClose(2)
                .build();

            var results = engine.evaluateAll(RulePhase.SETTLEMENT, context);
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(Satisfied.class::isInstance);
            assertThat(engine.hasBlockingViolations(RulePhase.SETTLEMENT, context)).isFalse();
        }

        @Test
        void realEstateMissingDocument_violated() {
            var context = RuleContext.builder()
                .productType("REAL_ESTATE")
                .fullPaymentReceived(true)
                .settlementDocumentProvided(false)
                .daysSinceClose(2)
                .build();

            assertThat(engine.hasBlockingViolations(RulePhase.SETTLEMENT, context)).isTrue();
            var violations = engine.violations(RulePhase.SETTLEMENT, context);
            assertThat(violations).anyMatch(v -> v.ruleName().equals(RuleName.of("REAL_ESTATE_DOCUMENT_3_DAYS")));
        }

        @Test
        void realEstateDocumentLate_violated() {
            var context = RuleContext.builder()
                .productType("REAL_ESTATE")
                .fullPaymentReceived(true)
                .settlementDocumentProvided(true)
                .daysSinceClose(5)
                .build();

            assertThat(engine.hasBlockingViolations(RulePhase.SETTLEMENT, context)).isTrue();
        }

        @Test
        void excisableProductWithOriginalDocument_satisfied() {
            var context = RuleContext.builder()
                .productType("ALCOHOL")
                .excisable(true)
                .fullPaymentReceived(true)
                .exciseDocumentStatus("ORIGINAL")
                .build();

            assertThat(engine.hasBlockingViolations(RulePhase.SETTLEMENT, context)).isFalse();
        }

        @Test
        void excisableProductWithCopyOnly_violated() {
            var context = RuleContext.builder()
                .productType("ALCOHOL")
                .excisable(true)
                .fullPaymentReceived(true)
                .exciseDocumentStatus("COPY")
                .build();

            assertThat(engine.hasBlockingViolations(RulePhase.SETTLEMENT, context)).isTrue();
        }
    }

    @Nested
    @DisplayName("RuleCatalog filtering")
    class RuleCatalogFiltering {

        @Test
        void rulesForPhaseReturnsOnlyMatchingPhase() {
            catalog.register(RuleDefinition.of(
                RuleName.of("PARTICIPATION_RULE"),
                RulePhase.PARTICIPATION,
                RuleSeverity.BLOCKING,
                new ConstantExpression(true),
                new ConstantExpression(true),
                "msg"
            ));
            catalog.register(RuleDefinition.of(
                RuleName.of("SETTLEMENT_RULE"),
                RulePhase.SETTLEMENT,
                RuleSeverity.BLOCKING,
                new ConstantExpression(true),
                new ConstantExpression(true),
                "msg"
            ));

            assertThat(catalog.rulesForPhase(RulePhase.PARTICIPATION)).hasSize(1);
            assertThat(catalog.rulesForPhase(RulePhase.SETTLEMENT)).hasSize(1);
            assertThat(catalog.rulesForPhase(RulePhase.BIDDING)).isEmpty();
        }

        @Test
        void applicableRulesFiltersByApplicabilityCondition() {
            catalog.register(RuleDefinition.of(
                RuleName.of("REAL_ESTATE_ONLY"),
                RulePhase.PARTICIPATION,
                RuleSeverity.BLOCKING,
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE"),
                new ConstantExpression(true),
                "msg"
            ));

            var realEstateContext = RuleContext.builder().productType("REAL_ESTATE").build();
            var vehicleContext = RuleContext.builder().productType("VEHICLE").build();

            assertThat(catalog.applicableRules(RulePhase.PARTICIPATION, realEstateContext)).hasSize(1);
            assertThat(catalog.applicableRules(RulePhase.PARTICIPATION, vehicleContext)).isEmpty();
        }
    }
}
