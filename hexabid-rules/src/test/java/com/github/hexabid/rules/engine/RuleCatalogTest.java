package com.github.hexabid.rules.engine;

import com.github.hexabid.rules.ast.AttributeCheck;
import com.github.hexabid.rules.ast.AttributeKey;
import com.github.hexabid.rules.ast.ConstantExpression;
import com.github.hexabid.rules.model.RuleContext;
import com.github.hexabid.rules.model.RuleName;
import com.github.hexabid.rules.model.RulePhase;
import com.github.hexabid.rules.model.RuleSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RuleCatalogTest {

    private RuleCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new RuleCatalog();
    }

    @Nested
    @DisplayName("Rule registration and retrieval")
    class RegistrationAndRetrieval {

        @Test
        void registerAddsRule() {
            catalog.register(participationRule());
            assertThat(catalog.allRules()).hasSize(1);
        }

        @Test
        void rulesForPhaseFiltersCorrectly() {
            catalog.register(participationRule());
            catalog.register(settlementRule());

            assertThat(catalog.rulesForPhase(RulePhase.PARTICIPATION)).hasSize(1);
            assertThat(catalog.rulesForPhase(RulePhase.SETTLEMENT)).hasSize(1);
            assertThat(catalog.rulesForPhase(RulePhase.BIDDING)).isEmpty();
        }

        @Test
        void allRulesReturnsAllRegistered() {
            catalog.register(participationRule());
            catalog.register(settlementRule());

            assertThat(catalog.allRules()).hasSize(2);
        }

        @Test
        void rulesForPhaseReturnsEmptyWhenNoneMatch() {
            catalog.register(participationRule());
            assertThat(catalog.rulesForPhase(RulePhase.BIDDING)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Applicability filtering")
    class ApplicabilityFiltering {

        @Test
        void applicableRulesFiltersByApplicabilityCondition() {
            catalog.register(realEstateRule());

            var realEstateCtx = RuleContext.builder().productType("REAL_ESTATE").build();
            var vehicleCtx = RuleContext.builder().productType("VEHICLE").build();

            assertThat(catalog.applicableRules(RulePhase.PARTICIPATION, realEstateCtx)).hasSize(1);
            assertThat(catalog.applicableRules(RulePhase.PARTICIPATION, vehicleCtx)).isEmpty();
        }

        @Test
        void applicableRulesFiltersByPhaseAndApplicability() {
            catalog.register(realEstateRule());
            catalog.register(settlementRule());

            var realEstateCtx = RuleContext.builder().productType("REAL_ESTATE").build();

            assertThat(catalog.applicableRules(RulePhase.PARTICIPATION, realEstateCtx)).hasSize(1);
            assertThat(catalog.applicableRules(RulePhase.SETTLEMENT, realEstateCtx)).hasSize(1);
        }

        @Test
        void alwaysApplicableRuleMatchesAllContexts() {
            catalog.register(alwaysApplicableRule());

            var anyContext = RuleContext.builder().productType("VEHICLE").build();
            assertThat(catalog.applicableRules(RulePhase.PARTICIPATION, anyContext)).hasSize(1);
        }

        @Test
        void neverApplicableRuleMatchesNoContexts() {
            catalog.register(neverApplicableRule());

            var anyContext = RuleContext.builder().productType("VEHICLE").build();
            assertThat(catalog.applicableRules(RulePhase.PARTICIPATION, anyContext)).isEmpty();
        }
    }

    private RuleDefinition participationRule() {
        return RuleDefinition.of(
            RuleName.of("PARTICIPATION_TEST"),
            RulePhase.PARTICIPATION,
            RuleSeverity.BLOCKING,
            new ConstantExpression(true),
            new ConstantExpression(true),
            "test"
        );
    }

    private RuleDefinition settlementRule() {
        return RuleDefinition.of(
            RuleName.of("SETTLEMENT_TEST"),
            RulePhase.SETTLEMENT,
            RuleSeverity.BLOCKING,
            new ConstantExpression(true),
            new ConstantExpression(true),
            "test"
        );
    }

    private RuleDefinition realEstateRule() {
        return RuleDefinition.of(
            RuleName.of("REAL_ESTATE_ONLY"),
            RulePhase.PARTICIPATION,
            RuleSeverity.BLOCKING,
            new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE"),
            new ConstantExpression(true),
            "test"
        );
    }

    private RuleDefinition alwaysApplicableRule() {
        return RuleDefinition.of(
            RuleName.of("ALWAYS"),
            RulePhase.PARTICIPATION,
            RuleSeverity.BLOCKING,
            new ConstantExpression(true),
            new ConstantExpression(true),
            "test"
        );
    }

    private RuleDefinition neverApplicableRule() {
        return RuleDefinition.of(
            RuleName.of("NEVER"),
            RulePhase.PARTICIPATION,
            RuleSeverity.BLOCKING,
            new ConstantExpression(false),
            new ConstantExpression(true),
            "test"
        );
    }
}
