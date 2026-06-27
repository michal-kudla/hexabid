package com.github.hexabid.pricing.calculator;

import com.github.hexabid.pricing.model.Money;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public final class PercentageCalculator implements PriceCalculator {

    private final String name;
    private final BigDecimal rate;

    public PercentageCalculator(String name, BigDecimal rate) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.rate = Objects.requireNonNull(rate, "rate must not be null");
        if (rate.signum() < 0) {
            throw new IllegalArgumentException("rate must not be negative");
        }
    }

    @Override
    public Money calculate(Map<String, Object> parameters) {
        Objects.requireNonNull(parameters, "parameters must not be null");
        Money baseAmount = (Money) parameters.get("baseAmount");
        Objects.requireNonNull(baseAmount, "parameter 'baseAmount' is required");
        return baseAmount.multiply(rate);
    }

    @Override
    public String name() { return name; }
}
