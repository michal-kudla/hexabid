package com.github.hexabid.quantity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UnitTest {

    @Test
    void shouldCreateUnit() {
        Unit unit = Unit.of("kg", "kilograms");

        assertEquals("kg", unit.symbol());
        assertEquals("kilograms", unit.name());
    }

    @Test
    void shouldRejectNullSymbol() {
        assertThrows(NullPointerException.class, () -> new Unit(null, "kilograms"));
    }

    @Test
    void shouldRejectBlankSymbol() {
        assertThrows(IllegalArgumentException.class, () -> new Unit("  ", "kilograms"));
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new Unit("kg", null));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Unit("kg", "  "));
    }

    @Test
    void shouldCreateCommonUnits() {
        assertEquals("pcs", Unit.pieces().symbol());
        assertEquals("kg", Unit.kilograms().symbol());
        assertEquals("l", Unit.liters().symbol());
        assertEquals("m", Unit.meters().symbol());
        assertEquals("m²", Unit.squareMeters().symbol());
        assertEquals("m³", Unit.cubicMeters().symbol());
        assertEquals("h", Unit.hours().symbol());
        assertEquals("min", Unit.minutes().symbol());
        assertEquals("pkg", Unit.packages().symbol());
    }

    @Test
    void shouldUseSymbolForToString() {
        assertEquals("kg", Unit.kilograms().toString());
    }
}
