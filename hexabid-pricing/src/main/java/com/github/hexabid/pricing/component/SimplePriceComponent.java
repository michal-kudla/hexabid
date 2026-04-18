package com.github.hexabid.pricing.component;

import com.github.hexabid.pricing.calculator.PriceCalculator;
import com.github.hexabid.pricing.model.Money;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class SimplePriceComponent implements PricingComponent {

    private final String name;
    private final PriceCalculator calculator;
    private final Map<String, String> parameterMappings;
    private final Map<String, Function<Map<String, Money>, Money>> dependentValueMappings;

    public SimplePriceComponent(String name,
                                PriceCalculator calculator,
                                Map<String, String> parameterMappings,
                                Map<String, Function<Map<String, Money>, Money>> dependentValueMappings) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.calculator = Objects.requireNonNull(calculator, "calculator must not be null");
        this.parameterMappings = Map.copyOf(parameterMappings);
        this.dependentValueMappings = Map.copyOf(dependentValueMappings);
    }

    public static SimplePriceComponent of(String name, PriceCalculator calculator) {
        return new SimplePriceComponent(name, calculator, Map.of(), Map.of());
    }

    public static Builder builder(String name, PriceCalculator calculator) {
        return new Builder(name, calculator);
    }

    @Override
    public ComponentResult calculate(Map<String, Money> dependentValues) {
        Map<String, Object> params = new HashMap<>();

        for (var entry : dependentValueMappings.entrySet()) {
            String paramKey = entry.getKey();
            Money resolvedValue = entry.getValue().apply(dependentValues);
            if (resolvedValue != null) {
                params.put(paramKey, resolvedValue);
            }
        }

        Money result = calculator.calculate(params);
        return ComponentResult.leaf(name, result);
    }

    @Override
    public String name() { return name; }

    public static class Builder {
        private final String name;
        private final PriceCalculator calculator;
        private final Map<String, String> parameterMappings = new HashMap<>();
        private final Map<String, Function<Map<String, Money>, Money>> dependentValueMappings = new LinkedHashMap<>();

        Builder(String name, PriceCalculator calculator) {
            this.name = name;
            this.calculator = calculator;
        }

        public Builder mapDependent(String paramKey, String dependentComponentName) {
            dependentValueMappings.put(paramKey, deps -> deps.get(dependentComponentName));
            return this;
        }

        public Builder mapDependentSum(String paramKey, String... dependentComponentNames) {
            dependentValueMappings.put(paramKey, deps -> {
                Money sum = null;
                for (String depName : dependentComponentNames) {
                    Money depValue = deps.get(depName);
                    if (depValue != null) {
                        sum = (sum == null) ? depValue : sum.add(depValue);
                    }
                }
                return sum;
            });
            return this;
        }

        public SimplePriceComponent build() {
            return new SimplePriceComponent(name, calculator, parameterMappings, dependentValueMappings);
        }
    }
}
