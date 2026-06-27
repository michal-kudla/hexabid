package com.github.hexabid.pricing.model;

import java.math.BigDecimal;
import java.util.Objects;

public record VatRate(BigDecimal rate) {

    public static final VatRate TWENTY_THREE = new VatRate(new BigDecimal("0.23"));
    public static final VatRate EIGHT = new VatRate(new BigDecimal("0.08"));
    public static final VatRate FIVE = new VatRate(new BigDecimal("0.05"));
    public static final VatRate ZERO = new VatRate(BigDecimal.ZERO);
    public static final VatRate EXEMPT = null;

    public VatRate {
        Objects.requireNonNull(rate, "rate must not be null");
        if (rate.signum() < 0) {
            throw new IllegalArgumentException("VAT rate must not be negative");
        }
        if (rate.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("VAT rate must be less than 1 (use 0.23, not 23)");
        }
    }

    public Money applyTo(Money base) {
        Objects.requireNonNull(base, "base must not be null");
        return base.multiply(rate);
    }
}
