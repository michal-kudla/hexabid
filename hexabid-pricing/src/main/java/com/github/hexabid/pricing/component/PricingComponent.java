package com.github.hexabid.pricing.component;

import com.github.hexabid.pricing.model.Money;
import java.util.Map;

public interface PricingComponent {

    ComponentResult calculate(Map<String, Money> dependentValues);

    String name();
}
