package com.acme.auctions.quantity;

import java.util.Objects;

public record Unit(String symbol, String name) {

    public Unit {
        Objects.requireNonNull(symbol, "symbol must not be null");
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }

    public static Unit of(String symbol, String name) {
        return new Unit(symbol, name);
    }

    public static Unit pieces() {
        return new Unit("pcs", "pieces");
    }

    public static Unit kilograms() {
        return new Unit("kg", "kilograms");
    }

    public static Unit liters() {
        return new Unit("l", "liters");
    }

    public static Unit meters() {
        return new Unit("m", "meters");
    }

    public static Unit squareMeters() {
        return new Unit("m²", "square meters");
    }

    public static Unit cubicMeters() {
        return new Unit("m³", "cubic meters");
    }

    public static Unit hours() {
        return new Unit("h", "hours");
    }

    public static Unit minutes() {
        return new Unit("min", "minutes");
    }

    public static Unit packages() {
        return new Unit("pkg", "packages");
    }

    @Override
    public String toString() {
        return symbol;
    }
}
