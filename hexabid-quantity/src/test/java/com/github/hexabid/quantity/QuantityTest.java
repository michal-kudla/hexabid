package com.github.hexabid.quantity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class QuantityTest {

    @Test
    void shouldCreateQuantity() {
        Quantity quantity = Quantity.of(new BigDecimal("100"), Unit.kilograms());

        assertEquals(new BigDecimal("100"), quantity.amount());
        assertEquals(Unit.kilograms(), quantity.unit());
    }

    @Test
    void shouldCreateQuantityFromDouble() {
        Quantity quantity = Quantity.of(25.5, Unit.kilograms());

        assertEquals(new BigDecimal("25.5"), quantity.amount());
    }

    @Test
    void shouldCreateQuantityFromInt() {
        Quantity quantity = Quantity.of(100, Unit.kilograms());

        assertEquals(new BigDecimal("100"), quantity.amount());
    }

    @Test
    void shouldAllowZeroQuantity() {
        Quantity quantity = Quantity.of(0, Unit.kilograms());

        assertEquals(BigDecimal.ZERO, quantity.amount());
    }

    @Test
    void shouldRejectNullAmount() {
        assertThrows(NullPointerException.class, () -> new Quantity(null, Unit.kilograms()));
    }

    @Test
    void shouldRejectNullUnit() {
        assertThrows(NullPointerException.class, () -> new Quantity(BigDecimal.TEN, null));
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> new Quantity(new BigDecimal("-1"), Unit.kilograms()));
    }

    @Test
    void shouldAddQuantitiesWithSameUnit() {
        Quantity q1 = Quantity.of(100, Unit.kilograms());
        Quantity q2 = Quantity.of(50, Unit.kilograms());

        Quantity result = q1.add(q2);

        assertEquals(Quantity.of(150, Unit.kilograms()), result);
    }

    @Test
    void shouldRejectAddWithDifferentUnits() {
        Quantity q1 = Quantity.of(100, Unit.kilograms());
        Quantity q2 = Quantity.of(50, Unit.liters());

        assertThrows(IllegalArgumentException.class, () -> q1.add(q2));
    }

    @Test
    void shouldSubtractQuantitiesWithSameUnit() {
        Quantity q1 = Quantity.of(100, Unit.kilograms());
        Quantity q2 = Quantity.of(30, Unit.kilograms());

        Quantity result = q1.subtract(q2);

        assertEquals(Quantity.of(70, Unit.kilograms()), result);
    }

    @Test
    void shouldRejectSubtractWithDifferentUnits() {
        Quantity q1 = Quantity.of(100, Unit.kilograms());
        Quantity q2 = Quantity.of(30, Unit.liters());

        assertThrows(IllegalArgumentException.class, () -> q1.subtract(q2));
    }

    @Test
    void shouldCompareQuantities() {
        Quantity q1 = Quantity.of(100, Unit.kilograms());
        Quantity q2 = Quantity.of(50, Unit.kilograms());
        Quantity q3 = Quantity.of(100, Unit.kilograms());

        assertTrue(q1.isGreaterThan(q2));
        assertFalse(q2.isGreaterThan(q1));
        assertTrue(q1.isGreaterThanOrEqualTo(q3));
        assertTrue(q1.isLessThan(Quantity.of(200, Unit.kilograms())));
    }

    @Test
    void shouldRejectCompareWithDifferentUnits() {
        Quantity q1 = Quantity.of(100, Unit.kilograms());
        Quantity q2 = Quantity.of(50, Unit.liters());

        assertThrows(IllegalArgumentException.class, () -> q1.isGreaterThan(q2));
    }

    @Test
    void shouldCompareTo() {
        Quantity q1 = Quantity.of(100, Unit.kilograms());
        Quantity q2 = Quantity.of(50, Unit.kilograms());
        Quantity q3 = Quantity.of(100, Unit.kilograms());

        assertTrue(q1.compareTo(q2) > 0);
        assertTrue(q2.compareTo(q1) < 0);
        assertEquals(0, q1.compareTo(q3));
    }

    @Test
    void shouldUseAmountAndUnitForToString() {
        Quantity quantity = Quantity.of(100, Unit.kilograms());

        assertEquals("100 kg", quantity.toString());
    }
}
