package com.github.hexabid.pricing.model;

import java.math.BigDecimal;
import java.util.Objects;

public record ExciseRate(BigDecimal rate, ExciseType type) {

    public enum ExciseType { PERCENTAGE, PER_UNIT }

    public static ExciseRate percentage(BigDecimal rate) {
        return new ExciseRate(rate, ExciseType.PERCENTAGE);
    }

    public static ExciseRate perUnit(BigDecimal ratePerUnit) {
        return new ExciseRate(ratePerUnit, ExciseType.PER_UNIT);
    }

    public ExciseRate {
        Objects.requireNonNull(rate, "rate must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (rate.signum() < 0) {
            throw new IllegalArgumentException("Excise rate must not be negative");
        }
        if (type == ExciseType.PERCENTAGE && rate.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("Percentage excise rate must be less than 1");
        }
    }

    public Money applyTo(Money base, BigDecimal quantity) {
        Objects.requireNonNull(base, "base must not be null");
        Objects.requireNonNull(quantity, "quantity must not be null");
        return switch (type) {
            case PERCENTAGE -> base.multiply(rate);
            case PER_UNIT -> base.currency().isEmpty() ? Money.zero("PLN") : new Money(rate.multiply(quantity), base.currency());
        };
    }
}
