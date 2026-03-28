package com.acme.auctions.payment.core.domain;

import com.acme.auctions.core.auctioning.model.Price;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

/**
 * Interface for currency conversion logic.
 */
public interface CurrencyConverter {
    Price convert(Price amount, String targetCurrency);
}
