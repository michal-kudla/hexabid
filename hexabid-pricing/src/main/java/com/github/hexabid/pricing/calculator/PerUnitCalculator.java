package com.github.hexabid.pricing.calculator;

import com.github.hexabid.pricing.model.Money;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public final class PerUnitCalculator implements PriceCalculator {

    private final String name;
    private final Money unitPrice;

    public PerUnitCalculator(String name, Money unitPrice) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice must not be null");
    }

    @Override
    public Money calculate(Map<String, Object> parameters) {
        Objects.requireNonNull(parameters, "parameters must not be null");
        BigDecimal quantity = (BigDecimal) parameters.get("quantity");
        Objects.requireNonNull(quantity, "parameter 'quantity' is required");
        return unitPrice.multiply(quantity);
    }

    @Override
    public String name() { return name; }
}
