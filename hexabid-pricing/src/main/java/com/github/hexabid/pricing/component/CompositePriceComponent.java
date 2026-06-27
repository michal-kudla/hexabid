package com.github.hexabid.pricing.component;

import com.github.hexabid.pricing.model.Money;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CompositePriceComponent implements PricingComponent {

    private final String name;
    private final List<PricingComponent> children;

    private CompositePriceComponent(String name, List<PricingComponent> children) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.children = List.copyOf(children);
    }

    public static CompositePriceComponent of(String name, List<PricingComponent> children) {
        return new CompositePriceComponent(name, children);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    public ComponentResult calculate(Map<String, Money> dependentValues) {
        Map<String, Money> computedValues = new LinkedHashMap<>(dependentValues);
        Map<String, ComponentResult> childResults = new LinkedHashMap<>();
        Money total = null;

        for (PricingComponent child : children) {
            ComponentResult childResult = child.calculate(computedValues);
            childResults.put(child.name(), childResult);
            computedValues.put(child.name(), childResult.value());
            total = (total == null) ? childResult.value() : total.add(childResult.value());
        }

        return ComponentResult.composite(name, total, childResults);
    }

    @Override
    public String name() { return name; }

    public static class Builder {
        private final String name;
        private final List<PricingComponent> children = new ArrayList<>();

        Builder(String name) { this.name = name; }

        public Builder child(PricingComponent child) {
            Objects.requireNonNull(child, "child must not be null");
            children.add(child);
            return this;
        }

        public CompositePriceComponent build() {
            return new CompositePriceComponent(name, children);
        }
    }
}
