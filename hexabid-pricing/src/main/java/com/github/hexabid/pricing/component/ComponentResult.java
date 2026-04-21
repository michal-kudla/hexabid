package com.github.hexabid.pricing.component;

import com.github.hexabid.pricing.model.Money;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ComponentResult {

    private final String name;
    private final Money value;
    private final Map<String, ComponentResult> children;

    private ComponentResult(String name, Money value, Map<String, ComponentResult> children) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.value = Objects.requireNonNull(value, "value must not be null");
        this.children = Collections.unmodifiableMap(children);
    }

    public static ComponentResult leaf(String name, Money value) {
        return new ComponentResult(name, value, Map.of());
    }

    public static ComponentResult composite(String name, Money value, Map<String, ComponentResult> children) {
        return new ComponentResult(name, value, children);
    }

    public String name() { return name; }
    public Money value() { return value; }
    public Map<String, ComponentResult> children() { return children; }

    @Override
    public String toString() {
        if (children.isEmpty()) {
            return "%s=%s".formatted(name, value);
        }
        return "%s=%s {%s}".formatted(name, value, children.values());
    }
}
