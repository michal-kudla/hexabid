package com.github.hexabid.pricing.calculator;

import com.github.hexabid.pricing.model.Money;
import com.github.hexabid.pricing.model.PricingContext;
import java.util.Map;

public interface PriceCalculator {

    Money calculate(Map<String, Object> parameters);

    String name();
}
