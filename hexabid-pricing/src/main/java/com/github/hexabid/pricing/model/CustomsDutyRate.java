package com.github.hexabid.pricing.model;

import java.math.BigDecimal;
import java.util.Objects;

public record CustomsDutyRate(BigDecimal rate) {

    public CustomsDutyRate {
        Objects.requireNonNull(rate, "rate must not be null");
        if (rate.signum() < 0) {
            throw new IllegalArgumentException("Customs duty rate must not be negative");
        }
        if (rate.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("Customs duty rate must be less than 1");
        }
    }

    public static CustomsDutyRate of(BigDecimal rate) {
        return new CustomsDutyRate(rate);
    }

    public static CustomsDutyRate zero() {
        return new CustomsDutyRate(BigDecimal.ZERO);
    }

    public Money applyTo(Money base) {
        Objects.requireNonNull(base, "base must not be null");
        return base.multiply(rate);
    }
}
