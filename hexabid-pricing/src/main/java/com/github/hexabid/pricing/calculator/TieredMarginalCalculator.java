package com.github.hexabid.pricing.calculator;

import com.github.hexabid.pricing.model.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class TieredMarginalCalculator implements PriceCalculator {

    private final String name;
    private final NavigableMap<BigDecimal, Money> tiers;

    private TieredMarginalCalculator(String name, NavigableMap<BigDecimal, Money> tiers) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.tiers = Objects.requireNonNull(tiers, "tiers must not be null");
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    public Money calculate(Map<String, Object> parameters) {
        Objects.requireNonNull(parameters, "parameters must not be null");
        BigDecimal quantity = (BigDecimal) parameters.get("quantity");
        Objects.requireNonNull(quantity, "parameter 'quantity' is required");
        Money unitPrice = tiers.floorEntry(quantity).getValue();
        return unitPrice.multiply(quantity);
    }

    @Override
    public String name() { return name; }

    public static class Builder {
        private final String name;
        private final NavigableMap<BigDecimal, Money> tiers = new TreeMap<>();

        Builder(String name) { this.name = name; }

        public Builder tier(BigDecimal fromQuantity, Money unitPrice) {
            Objects.requireNonNull(fromQuantity, "fromQuantity must not be null");
            Objects.requireNonNull(unitPrice, "unitPrice must not be null");
            tiers.put(fromQuantity, unitPrice);
            return this;
        }

        public TieredMarginalCalculator build() {
            if (tiers.isEmpty()) {
                throw new IllegalStateException("At least one tier is required");
            }
            if (!tiers.containsKey(BigDecimal.ZERO)) {
                throw new IllegalStateException("First tier must start from quantity 0");
            }
            return new TieredMarginalCalculator(name, tiers);
        }
    }
}
