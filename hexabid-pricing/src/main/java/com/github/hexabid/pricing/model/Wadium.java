package com.github.hexabid.pricing.model;

import java.math.BigDecimal;
import java.util.Objects;

public sealed interface Wadium permits FixedWadium, PercentageWadium {

    Money calculate(Money base);

    Money amountPaid();

    static Wadium fixed(Money amount) {
        return new FixedWadium(amount);
    }

    static Wadium percentage(BigDecimal rate, Money referencePrice) {
        return new PercentageWadium(rate, referencePrice);
    }
}

record FixedWadium(Money amount) implements Wadium {

    public FixedWadium {
        Objects.requireNonNull(amount, "amount must not be null");
    }

    @Override
    public Money calculate(Money base) {
        return amount;
    }

    @Override
    public Money amountPaid() {
        return amount;
    }
}

record PercentageWadium(BigDecimal rate, Money referencePrice) implements Wadium {

    public PercentageWadium {
        Objects.requireNonNull(rate, "rate must not be null");
        Objects.requireNonNull(referencePrice, "referencePrice must not be null");
        if (rate.signum() <= 0) {
            throw new IllegalArgumentException("Wadium rate must be positive");
        }
        if (rate.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("Wadium rate must be less than 1");
        }
    }

    @Override
    public Money calculate(Money base) {
        return referencePrice.multiply(rate);
    }

    @Override
    public Money amountPaid() {
        return referencePrice.multiply(rate);
    }
}
