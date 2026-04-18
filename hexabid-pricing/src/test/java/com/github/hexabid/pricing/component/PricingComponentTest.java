package com.github.hexabid.pricing.component;

import com.github.hexabid.pricing.calculator.PercentageCalculator;
import com.github.hexabid.pricing.calculator.SimpleFixedCalculator;
import com.github.hexabid.pricing.model.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PricingComponentTest {

    @Nested
    class SimpleComponentTests {
        @Test
        void shouldReturnLeafResult() {
            var comp = SimplePriceComponent.of("session-fee",
                new SimpleFixedCalculator("fee", Money.of("1.50", "PLN")));
            ComponentResult result = comp.calculate(Map.of());
            assertEquals("session-fee", result.name());
            assertEquals(Money.of("1.50", "PLN"), result.value());
            assertTrue(result.children().isEmpty());
        }

        @Test
        void shouldResolveDependentValue() {
            var vatComp = SimplePriceComponent.builder("vat",
                    new PercentageCalculator("vat", new BigDecimal("0.23")))
                .mapDependent("baseAmount", "netto")
                .build();

            ComponentResult result = vatComp.calculate(Map.of("netto", Money.of("1000.00", "PLN")));
            assertEquals(Money.of("230.00", "PLN"), result.value());
        }

        @Test
        void shouldResolveDependentSum() {
            var vatComp = SimplePriceComponent.builder("vat",
                    new PercentageCalculator("vat", new BigDecimal("0.23")))
                .mapDependentSum("baseAmount", "netto", "excise")
                .build();

            ComponentResult result = vatComp.calculate(Map.of(
                "netto", Money.of("1000.00", "PLN"),
                "excise", Money.of("100.00", "PLN")
            ));
            assertEquals(Money.of("253.00", "PLN"), result.value());
        }
    }

    @Nested
    class CompositeComponentTests {
        @Test
        void shouldSumChildrenValues() {
            var child1 = SimplePriceComponent.of("a", new SimpleFixedCalculator("a", Money.of("10.00", "PLN")));
            var child2 = SimplePriceComponent.of("b", new SimpleFixedCalculator("b", Money.of("20.00", "PLN")));

            var composite = CompositePriceComponent.of("total", java.util.List.of(child1, child2));
            ComponentResult result = composite.calculate(Map.of());

            assertEquals(Money.of("30.00", "PLN"), result.value());
            assertEquals(2, result.children().size());
        }

        @Test
        void shouldFeedChildResultsToLaterChildren() {
            var hammer = SimplePriceComponent.of("hammer-price",
                new SimpleFixedCalculator("h", Money.of("100.00", "PLN")));

            Money nettoValue = Money.of("100.00", "PLN").subtract(Money.of("10.00", "PLN"));
            var netto = SimplePriceComponent.of("netto",
                new SimpleFixedCalculator("n", nettoValue));

            var vat = SimplePriceComponent.builder("vat",
                    new PercentageCalculator("vat", new BigDecimal("0.23")))
                .mapDependent("baseAmount", "netto")
                .build();

            var root = CompositePriceComponent.builder("total")
                .child(hammer)
                .child(netto)
                .child(vat)
                .build();

            ComponentResult result = root.calculate(Map.of());

            assertEquals(Money.of("90.00", "PLN"), result.children().get("netto").value());
            assertEquals(Money.of("20.70", "PLN"), result.children().get("vat").value());
        }

        @Test
        void shouldBuildHierarchicalBreakdown() {
            var energy = SimplePriceComponent.of("energy",
                new SimpleFixedCalculator("e", Money.of("50.00", "PLN")));
            var markup = SimplePriceComponent.of("markup",
                new SimpleFixedCalculator("m", Money.of("10.00", "PLN")));
            var netto = CompositePriceComponent.of("netto", java.util.List.of(energy, markup));

            var vat = SimplePriceComponent.builder("vat",
                    new PercentageCalculator("vat", new BigDecimal("0.23")))
                .mapDependent("baseAmount", "netto")
                .build();

            var root = CompositePriceComponent.builder("total")
                .child(netto)
                .child(vat)
                .build();

            ComponentResult result = root.calculate(Map.of());

            ComponentResult nettoResult = result.children().get("netto");
            assertEquals(Money.of("60.00", "PLN"), nettoResult.value());
            assertEquals(Money.of("13.80", "PLN"), result.children().get("vat").value());
            assertEquals(Money.of("73.80", "PLN"), result.value());
        }
    }
}
