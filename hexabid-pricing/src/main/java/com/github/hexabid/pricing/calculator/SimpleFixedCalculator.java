package com.github.hexabid.pricing.calculator;

import com.github.hexabid.pricing.model.Money;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public final class SimpleFixedCalculator implements PriceCalculator {

    private final String name;
    private final Money amount;

    public SimpleFixedCalculator(String name, Money amount) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
    }

    @Override
    public Money calculate(Map<String, Object> parameters) {
        return amount;
    }

    @Override
    public String name() { return name; }
}
