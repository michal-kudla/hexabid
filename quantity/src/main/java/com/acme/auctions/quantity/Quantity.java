package com.acme.auctions.quantity;

import java.math.BigDecimal;
import java.util.Objects;

public record Quantity(BigDecimal amount, Unit unit) implements Comparable<Quantity> {

    public Quantity {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(unit, "unit must not be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }

    public static Quantity of(BigDecimal amount, Unit unit) {
        return new Quantity(amount, unit);
    }

    public static Quantity of(double amount, Unit unit) {
        return new Quantity(BigDecimal.valueOf(amount), unit);
    }

    public static Quantity of(int amount, Unit unit) {
        return new Quantity(BigDecimal.valueOf(amount), unit);
    }

    public Quantity add(Quantity other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException(
                String.format("Cannot add quantities with different units: %s and %s", this.unit, other.unit));
        }
        return new Quantity(this.amount.add(other.amount), this.unit);
    }

    public Quantity subtract(Quantity other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException(
                String.format("Cannot subtract quantities with different units: %s and %s", this.unit, other.unit));
        }
        return new Quantity(this.amount.subtract(other.amount), this.unit);
    }

    public boolean isGreaterThan(Quantity other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException(
                String.format("Cannot compare quantities with different units: %s and %s", this.unit, other.unit));
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqualTo(Quantity other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException(
                String.format("Cannot compare quantities with different units: %s and %s", this.unit, other.unit));
        }
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThan(Quantity other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException(
                String.format("Cannot compare quantities with different units: %s and %s", this.unit, other.unit));
        }
        return this.amount.compareTo(other.amount) < 0;
    }

    @Override
    public int compareTo(Quantity other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException(
                String.format("Cannot compare quantities with different units: %s and %s", this.unit, other.unit));
        }
        return this.amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        return amount + " " + unit;
    }
}
