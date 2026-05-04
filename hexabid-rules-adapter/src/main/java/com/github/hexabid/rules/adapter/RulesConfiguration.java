package com.github.hexabid.rules.adapter;

import com.github.hexabid.core.auctioning.port.out.AuctionRuleEvaluator;
import com.github.hexabid.core.auctioning.port.out.DocumentRepository;
import com.github.hexabid.core.auctioning.port.out.WadiumDepositPort;
import com.github.hexabid.rules.ast.AndExpression;
import com.github.hexabid.rules.ast.AttributeCheck;
import com.github.hexabid.rules.ast.AttributeKey;
import com.github.hexabid.rules.ast.ConstantExpression;
import com.github.hexabid.rules.ast.MetricComparison;
import com.github.hexabid.rules.ast.NotExpression;
import com.github.hexabid.rules.ast.OrExpression;
import com.github.hexabid.rules.engine.RuleCatalog;
import com.github.hexabid.rules.engine.RuleDefinition;
import com.github.hexabid.rules.engine.RuleEngine;
import com.github.hexabid.rules.model.RuleName;
import com.github.hexabid.rules.model.RulePhase;
import com.github.hexabid.rules.model.RuleSeverity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class RulesConfiguration {

    @Bean
    public RuleCatalog auctionRuleCatalog() {
        var catalog = new RuleCatalog();

        catalog.register(RuleDefinition.of(
            RuleName.of("WADIUM_10_PERCENT_ABOVE_10K"),
            RulePhase.PARTICIPATION,
            RuleSeverity.BLOCKING,
            new AndExpression(
                new MetricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.GREATER_THAN, new BigDecimal("10000")),
                new NotExpression(new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE"))
            ),
            new AttributeCheck(AttributeKey.WADIUM_PAID, AttributeCheck.Comparator.EQUALS, true),
            "Wadium 10% wartosci szacunkowej jest wymagane dla produktow o wartosci powyzej 10000 PLN"
        ));

        catalog.register(RuleDefinition.of(
            RuleName.of("WADIUM_REAL_ESTATE"),
            RulePhase.PARTICIPATION,
            RuleSeverity.BLOCKING,
            new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE"),
            new AttributeCheck(AttributeKey.WADIUM_PAID, AttributeCheck.Comparator.EQUALS, true),
            "Dla nieruchomosci wadium 10% jest wymagane przed licytacja"
        ));

        catalog.register(RuleDefinition.of(
            RuleName.of("VIEWING_DATE_REAL_ESTATE"),
            RulePhase.PARTICIPATION,
            RuleSeverity.BLOCKING,
            new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE"),
            new AttributeCheck(AttributeKey.VIEWING_DATE_SCHEDULED, AttributeCheck.Comparator.EQUALS, true),
            "Dla nieruchomosci wymagane jest zdefiniowanie terminu ogledzin"
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
            "Wymagana pelnoletnosc do zakupu alkoholu lub tytoniu"
        ));

        catalog.register(RuleDefinition.of(
            RuleName.of("EXCISE_COPY_PARTICIPATION"),
            RulePhase.PARTICIPATION,
            RuleSeverity.WARNING,
            new AttributeCheck(AttributeKey.EXCISABLE, AttributeCheck.Comparator.EQUALS, true),
            new OrExpression(
                new AttributeCheck(AttributeKey.EXCISE_DOCUMENT_STATUS, AttributeCheck.Comparator.EQUALS, "COPY"),
                new AttributeCheck(AttributeKey.EXCISE_DOCUMENT_STATUS, AttributeCheck.Comparator.EQUALS, "ORIGINAL")
            ),
            "Dostarcz kopie dokumentu akcyzy. Oryginal bedzie wymagany po wygranej"
        ));

        catalog.register(RuleDefinition.of(
            RuleName.of("CUSTOMS_EXEMPTION_COPY_PARTICIPATION"),
            RulePhase.PARTICIPATION,
            RuleSeverity.WARNING,
            new AndExpression(
                new AttributeCheck(AttributeKey.IMPORTED, AttributeCheck.Comparator.EQUALS, true),
                new AttributeCheck(AttributeKey.CUSTOMS_EXEMPT, AttributeCheck.Comparator.EQUALS, true)
            ),
            new OrExpression(
                new AttributeCheck(AttributeKey.CUSTOMS_EXEMPTION_DOC_STATUS, AttributeCheck.Comparator.EQUALS, "COPY"),
                new AttributeCheck(AttributeKey.CUSTOMS_EXEMPTION_DOC_STATUS, AttributeCheck.Comparator.EQUALS, "ORIGINAL")
            ),
            "Dostarcz kopie zwolnienia celnego. Oryginal bedzie wymagany po wygranej"
        ));

        catalog.register(RuleDefinition.of(
            RuleName.of("WADIUM_PAID_FOR_BIDDING"),
            RulePhase.BIDDING,
            RuleSeverity.BLOCKING,
            new OrExpression(
                new MetricComparison(AttributeKey.ESTIMATED_VALUE, AttributeCheck.Comparator.GREATER_THAN, new BigDecimal("10000")),
                new AttributeCheck(AttributeKey.PRODUCT_TYPE, AttributeCheck.Comparator.EQUALS, "REAL_ESTATE")
            ),
            new AttributeCheck(AttributeKey.WADIUM_PAID, AttributeCheck.Comparator.EQUALS, true),
            "Nie mozna licytowac bez wplate wadium"
        ));

        catalog.register(RuleDefinition.of(
            RuleName.of("KYC_VERIFIED_FOR_BIDDING"),
            RulePhase.BIDDING,
            RuleSeverity.BLOCKING,
            new ConstantExpression(true),
            new AttributeCheck(AttributeKey.KYC_VERIFIED, AttributeCheck.Comparator.EQUALS, true),
            "Wymagana weryfikacja KYC"
        ));

        catalog.register(RuleDefinition.of(
            RuleName.of("EXCISE_ORIGINAL_SETTLEMENT"),
            RulePhase.SETTLEMENT,
            RuleSeverity.BLOCKING,
            new AttributeCheck(AttributeKey.EXCISABLE, AttributeCheck.Comparator.EQUALS, true),
            new AttributeCheck(AttributeKey.EXCISE_DOCUMENT_STATUS, AttributeCheck.Comparator.EQUALS, "ORIGINAL"),
            "Wymagany oryginal dokumentu akcyzy do rozliczenia"
        ));

        catalog.register(RuleDefinition.of(
            RuleName.of("CUSTOMS_EXEMPTION_ORIGINAL_SETTLEMENT"),
            RulePhase.SETTLEMENT,
            RuleSeverity.BLOCKING,
            new AndExpression(
                new AttributeCheck(AttributeKey.IMPORTED, AttributeCheck.Comparator.EQUALS, true),
                new AttributeCheck(AttributeKey.CUSTOMS_EXEMPT, AttributeCheck.Comparator.EQUALS, true)
            ),
            new AttributeCheck(AttributeKey.CUSTOMS_EXEMPTION_DOC_STATUS, AttributeCheck.Comparator.EQUALS, "ORIGINAL"),
            "Wymagany oryginal zwolnienia celnego do rozliczenia"
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
            "Dla nieruchomosci wymagany oryginal dokumentu w ciagu 3 dni roboczych"
        ));

        catalog.register(RuleDefinition.of(
            RuleName.of("FULL_PAYMENT_SETTLEMENT"),
            RulePhase.SETTLEMENT,
            RuleSeverity.BLOCKING,
            new ConstantExpression(true),
            new AttributeCheck(AttributeKey.FULL_PAYMENT_RECEIVED, AttributeCheck.Comparator.EQUALS, true),
            "Wymagana platnosc pelnej kwoty do rozliczenia"
        ));

        return catalog;
    }

    @Bean
    public RuleEngine ruleEngine(RuleCatalog auctionRuleCatalog) {
        return new RuleEngine(auctionRuleCatalog);
    }

    @Bean
    public AuctionRuleEvaluator auctionRuleEvaluator(
            RuleEngine ruleEngine,
            DocumentRepository documentRepository,
            WadiumDepositPort wadiumDepositPort
    ) {
        return new RuleEvaluatorAdapter(ruleEngine, documentRepository, wadiumDepositPort);
    }
}
