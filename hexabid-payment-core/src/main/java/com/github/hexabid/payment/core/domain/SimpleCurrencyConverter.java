package com.github.hexabid.payment.core.domain;

import com.github.hexabid.core.auctioning.model.Price;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple implementation of CurrencyConverter with hardcoded rates for demonstration.
 */
public class SimpleCurrencyConverter implements CurrencyConverter {

    private final Map<String, BigDecimal> ratesToUsd = new ConcurrentHashMap<>();

    public SimpleCurrencyConverter() {
        ratesToUsd.put("USD", BigDecimal.ONE);
        ratesToUsd.put("PLN", new BigDecimal("0.25")); // 1 PLN = 0.25 USD
        ratesToUsd.put("BTC", new BigDecimal("60000")); // 1 BTC = 60000 USD
    }

    @Override
    public Price convert(Price amount, String targetCurrency) {
        if (amount.currency().equals(targetCurrency)) {
            return amount;
        }

        BigDecimal amountInUsd = amount.amount().multiply(ratesToUsd.getOrDefault(amount.currency(), BigDecimal.ONE));
        BigDecimal targetRate = ratesToUsd.getOrDefault(targetCurrency, BigDecimal.ONE);
        BigDecimal convertedAmount = amountInUsd.divide(targetRate, 8, RoundingMode.HALF_UP);

        return new Price(convertedAmount, targetCurrency);
    }
}
