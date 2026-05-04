package com.github.hexabid.rules.model;

import com.github.hexabid.rules.ast.AttributeKey;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RuleContext {

    private final Map<AttributeKey, Object> attributes;

    private RuleContext(Map<AttributeKey, Object> attributes) {
        this.attributes = Collections.unmodifiableMap(new HashMap<>(attributes));
    }

    public static Builder builder() {
        return new Builder();
    }

    @org.jspecify.annotations.Nullable
    public Object get(AttributeKey key) {
        return attributes.get(key);
    }

    public String getString(AttributeKey key) {
        var val = attributes.get(key);
        return val instanceof String s ? s : null;
    }

    public BigDecimal getBigDecimal(AttributeKey key) {
        var val = attributes.get(key);
        return val instanceof BigDecimal bd ? bd : null;
    }

    public Integer getInteger(AttributeKey key) {
        var val = attributes.get(key);
        return val instanceof Integer i ? i : null;
    }

    public Boolean getBoolean(AttributeKey key) {
        var val = attributes.get(key);
        return val instanceof Boolean b ? b : null;
    }

    public boolean has(AttributeKey key) {
        return attributes.containsKey(key);
    }

    public Map<AttributeKey, Object> attributes() {
        return attributes;
    }

    public static final class Builder {
        private final Map<AttributeKey, Object> attributes = new HashMap<>();

        public Builder with(AttributeKey key, Object value) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
            attributes.put(key, value);
            return this;
        }

        public Builder productType(String type) {
            return with(AttributeKey.PRODUCT_TYPE, type);
        }

        public Builder estimatedValue(BigDecimal value) {
            return with(AttributeKey.ESTIMATED_VALUE, value);
        }

        public Builder estimatedValue(long value) {
            return with(AttributeKey.ESTIMATED_VALUE, BigDecimal.valueOf(value));
        }

        public Builder bidderAge(Integer age) {
            return with(AttributeKey.BIDDER_AGE, age);
        }

        public Builder wadiumPaid(boolean paid) {
            return with(AttributeKey.WADIUM_PAID, paid);
        }

        public Builder excisable(boolean excisable) {
            return with(AttributeKey.EXCISABLE, excisable);
        }

        public Builder imported(boolean imported) {
            return with(AttributeKey.IMPORTED, imported);
        }

        public Builder customsExempt(boolean exempt) {
            return with(AttributeKey.CUSTOMS_EXEMPT, exempt);
        }

        public Builder kycVerified(boolean verified) {
            return with(AttributeKey.KYC_VERIFIED, verified);
        }

        public Builder viewingDateScheduled(boolean scheduled) {
            return with(AttributeKey.VIEWING_DATE_SCHEDULED, scheduled);
        }

        public Builder exciseDocumentStatus(String status) {
            return with(AttributeKey.EXCISE_DOCUMENT_STATUS, status);
        }

        public Builder customsExemptionDocStatus(String status) {
            return with(AttributeKey.CUSTOMS_EXEMPTION_DOC_STATUS, status);
        }

        public Builder settlementDocumentProvided(boolean provided) {
            return with(AttributeKey.SETTLEMENT_DOCUMENT_PROVIDED, provided);
        }

        public Builder daysSinceClose(int days) {
            return with(AttributeKey.DAYS_SINCE_CLOSE, days);
        }

        public Builder fullPaymentReceived(boolean received) {
            return with(AttributeKey.FULL_PAYMENT_RECEIVED, received);
        }

        public Builder receiptConfirmed(boolean confirmed) {
            return with(AttributeKey.RECEIPT_CONFIRMED, confirmed);
        }

        public RuleContext build() {
            return new RuleContext(attributes);
        }
    }
}
