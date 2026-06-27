package com.github.hexabid.pricing.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class VatRateTest {

    @Test
    void shouldApplyVatToBase() {
        VatRate rate = VatRate.TWENTY_THREE;
        Money base = Money.of("1000.00", "PLN");
        assertEquals(Money.of("230.00", "PLN"), rate.applyTo(base));
    }

    @Test
    void shouldApplyFivePercentVat() {
        Money base = Money.of("250.00", "PLN");
        assertEquals(Money.of("12.50", "PLN"), VatRate.FIVE.applyTo(base));
    }

    @Test
    void shouldApplyZeroVat() {
        Money base = Money.of("100.00", "PLN");
        assertEquals(Money.of("0.00", "PLN"), VatRate.ZERO.applyTo(base));
    }

    @Test
    void shouldRejectRateAboveOne() {
        assertThrows(IllegalArgumentException.class,
            () -> new VatRate(new BigDecimal("23")));
    }

    @Test
    void shouldRejectNegativeRate() {
        assertThrows(IllegalArgumentException.class,
            () -> new VatRate(new BigDecimal("-0.01")));
    }
}
