package com.github.hexabid.pricing.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class WadiumTest {

    @Test
    void shouldCalculateFixedWadium() {
        Wadium wadium = Wadium.fixed(Money.of("2000.00", "PLN"));
        Money base = Money.of("50000.00", "PLN");
        assertEquals(Money.of("2000.00", "PLN"), wadium.calculate(base));
    }

    @Test
    void shouldCalculatePercentageWadium() {
        Wadium wadium = Wadium.percentage(new BigDecimal("0.05"), Money.of("40000.00", "PLN"));
        Money base = Money.of("50000.00", "PLN");
        assertEquals(Money.of("2000.00", "PLN"), wadium.calculate(base));
    }

    @Test
    void shouldReportAmountPaidForFixed() {
        Wadium wadium = Wadium.fixed(Money.of("50.00", "PLN"));
        assertEquals(Money.of("50.00", "PLN"), wadium.amountPaid());
    }

    @Test
    void shouldReportAmountPaidForPercentage() {
        Wadium wadium = Wadium.percentage(new BigDecimal("0.10"), Money.of("1000.00", "PLN"));
        assertEquals(Money.of("100.00", "PLN"), wadium.amountPaid());
    }
}
