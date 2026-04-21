package com.github.hexabid.pricing.calculator;

import com.github.hexabid.pricing.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PriceCalculatorTest {

    @Test
    void simpleFixedShouldReturnConstantAmount() {
        var calc = new SimpleFixedCalculator("test", Money.of("150.00", "PLN"));
        assertEquals(Money.of("150.00", "PLN"), calc.calculate(Map.of()));
    }

    @Test
    void simpleFixedShouldIgnoreParameters() {
        var calc = new SimpleFixedCalculator("test", Money.of("150.00", "PLN"));
        assertEquals(Money.of("150.00", "PLN"), calc.calculate(Map.of("irrelevant", "data")));
    }

    @Test
    void percentageShouldCalculateFromBase() {
        var calc = new PercentageCalculator("vat", new BigDecimal("0.23"));
        Money result = calc.calculate(Map.of("baseAmount", Money.of("1000.00", "PLN")));
        assertEquals(Money.of("230.00", "PLN"), result);
    }

    @Test
    void percentageShouldRequireBaseAmount() {
        var calc = new PercentageCalculator("vat", new BigDecimal("0.23"));
        assertThrows(NullPointerException.class, () -> calc.calculate(Map.of()));
    }

    @Test
    void perUnitShouldCalculateTotalFromQuantity() {
        var calc = new PerUnitCalculator("per-kg", Money.of("3.00", "PLN"));
        Money result = calc.calculate(Map.of("quantity", new BigDecimal("100")));
        assertEquals(Money.of("300.00", "PLN"), result);
    }

    @Test
    void perUnitShouldRequireQuantity() {
        var calc = new PerUnitCalculator("per-kg", Money.of("3.00", "PLN"));
        assertThrows(NullPointerException.class, () -> calc.calculate(Map.of()));
    }

    @Test
    void tieredShouldReturnFirstTierPrice() {
        var calc = TieredMarginalCalculator.builder("tiered")
            .tier(BigDecimal.ZERO, Money.of("5.00", "PLN"))
            .tier(new BigDecimal("10"), Money.of("4.00", "PLN"))
            .build();
        Money result = calc.calculate(Map.of("quantity", new BigDecimal("5")));
        assertEquals(Money.of("25.00", "PLN"), result);
    }

    @Test
    void tieredShouldReturnSecondTierPrice() {
        var calc = TieredMarginalCalculator.builder("tiered")
            .tier(BigDecimal.ZERO, Money.of("5.00", "PLN"))
            .tier(new BigDecimal("10"), Money.of("4.00", "PLN"))
            .build();
        Money result = calc.calculate(Map.of("quantity", new BigDecimal("15")));
        assertEquals(Money.of("60.00", "PLN"), result);
    }
}
