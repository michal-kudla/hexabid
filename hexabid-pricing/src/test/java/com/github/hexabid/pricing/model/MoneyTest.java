package com.github.hexabid.pricing.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Nested
    class Creation {
        @Test
        void shouldCreateFromBigDecimal() {
            Money m = Money.of(new BigDecimal("100.50"), "PLN");
            assertEquals(new BigDecimal("100.50"), m.amount());
            assertEquals("PLN", m.currency());
        }

        @Test
        void shouldCreateFromString() {
            Money m = Money.of("200.00", "EUR");
            assertEquals(new BigDecimal("200.00"), m.amount());
            assertEquals("EUR", m.currency());
        }

        @Test
        void shouldCreateZero() {
            Money m = Money.zero("PLN");
            assertEquals(new BigDecimal("0.00"), m.amount());
            assertTrue(m.isZero());
        }

        @Test
        void shouldRejectNegativeAmount() {
            assertThrows(IllegalArgumentException.class,
                () -> Money.of(new BigDecimal("-1"), "PLN"));
        }

        @Test
        void shouldRejectBlankCurrency() {
            assertThrows(IllegalArgumentException.class,
                () -> Money.of(BigDecimal.TEN, "  "));
        }

        @Test
        void shouldNormalizeCurrencyToUppercase() {
            Money m = Money.of(BigDecimal.TEN, "pln");
            assertEquals("PLN", m.currency());
        }

        @Test
        void shouldScaleAmountToTwoDecimalPlaces() {
            Money m = Money.of(new BigDecimal("100.555"), "PLN");
            assertEquals(new BigDecimal("100.56"), m.amount());
        }
    }

    @Nested
    class Arithmetic {
        @Test
        void shouldAddSameCurrency() {
            Money a = Money.of("100.00", "PLN");
            Money b = Money.of("50.25", "PLN");
            assertEquals(Money.of("150.25", "PLN"), a.add(b));
        }

        @Test
        void shouldSubtractSameCurrency() {
            Money a = Money.of("100.00", "PLN");
            Money b = Money.of("30.00", "PLN");
            assertEquals(Money.of("70.00", "PLN"), a.subtract(b));
        }

        @Test
        void shouldMultiplyByBigDecimal() {
            Money m = Money.of("100.00", "PLN");
            assertEquals(Money.of("23.00", "PLN"), m.multiply(new BigDecimal("0.23")));
        }

        @Test
        void shouldMultiplyByInt() {
            Money m = Money.of("3.00", "PLN");
            assertEquals(Money.of("300.00", "PLN"), m.multiply(100));
        }

        @Test
        void shouldRejectCurrencyMismatchOnAdd() {
            Money pln = Money.of("100", "PLN");
            Money eur = Money.of("50", "EUR");
            assertThrows(IllegalArgumentException.class, () -> pln.add(eur));
        }

        @Test
        void shouldRejectCurrencyMismatchOnSubtract() {
            Money pln = Money.of("100", "PLN");
            Money eur = Money.of("50", "EUR");
            assertThrows(IllegalArgumentException.class, () -> pln.subtract(eur));
        }
    }

    @Nested
    class Comparison {
        @Test
        void shouldDetectGreaterThan() {
            Money big = Money.of("101", "PLN");
            Money small = Money.of("100", "PLN");
            assertTrue(big.isGreaterThan(small));
            assertFalse(small.isGreaterThan(big));
        }
    }
}
