package com.github.hexabid.pricing.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

public final class PricingContext {

    private final Money hammerPrice;
    private final BigDecimal quantity;
    private final String productType;
    private final boolean excisable;
    private final boolean imported;
    private final @Nullable VatRate vatRate;
    private final @Nullable ExciseRate exciseRate;
    private final @Nullable CustomsDutyRate customsDutyRate;
    private final @Nullable Wadium wadium;
    private final @Nullable String hsCode;
    private final @Nullable String originCountry;

    private PricingContext(Builder builder) {
        this.hammerPrice = Objects.requireNonNull(builder.hammerPrice, "hammerPrice must not be null");
        this.quantity = Objects.requireNonNull(builder.quantity, "quantity must not be null");
        this.productType = Objects.requireNonNull(builder.productType, "productType must not be null");
        this.excisable = builder.excisable;
        this.imported = builder.imported;
        this.vatRate = builder.vatRate;
        this.exciseRate = builder.exciseRate;
        this.customsDutyRate = builder.customsDutyRate;
        this.wadium = builder.wadium;
        this.hsCode = builder.hsCode;
        this.originCountry = builder.originCountry;
    }

    public Money hammerPrice() { return hammerPrice; }
    public BigDecimal quantity() { return quantity; }
    public String productType() { return productType; }
    public boolean isExcisable() { return excisable; }
    public boolean isImported() { return imported; }
    public Optional<VatRate> maybeVatRate() { return Optional.ofNullable(vatRate); }
    public Optional<ExciseRate> maybeExciseRate() { return Optional.ofNullable(exciseRate); }
    public Optional<CustomsDutyRate> maybeCustomsDutyRate() { return Optional.ofNullable(customsDutyRate); }
    public Optional<Wadium> maybeWadium() { return Optional.ofNullable(wadium); }
    public Optional<String> maybeHsCode() { return Optional.ofNullable(hsCode); }
    public Optional<String> maybeOriginCountry() { return Optional.ofNullable(originCountry); }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Money hammerPrice;
        private BigDecimal quantity = BigDecimal.ONE;
        private String productType = "UNIQUE";
        private boolean excisable;
        private boolean imported;
        private VatRate vatRate;
        private ExciseRate exciseRate;
        private CustomsDutyRate customsDutyRate;
        private Wadium wadium;
        private String hsCode;
        private String originCountry;

        public Builder hammerPrice(Money hammerPrice) { this.hammerPrice = hammerPrice; return this; }
        public Builder quantity(BigDecimal quantity) { this.quantity = quantity; return this; }
        public Builder productType(String productType) { this.productType = productType; return this; }
        public Builder excisable(boolean excisable) { this.excisable = excisable; return this; }
        public Builder imported(boolean imported) { this.imported = imported; return this; }
        public Builder vatRate(VatRate vatRate) { this.vatRate = vatRate; return this; }
        public Builder exciseRate(ExciseRate exciseRate) { this.exciseRate = exciseRate; return this; }
        public Builder customsDutyRate(CustomsDutyRate customsDutyRate) { this.customsDutyRate = customsDutyRate; return this; }
        public Builder wadium(Wadium wadium) { this.wadium = wadium; return this; }
        public Builder hsCode(String hsCode) { this.hsCode = hsCode; return this; }
        public Builder originCountry(String originCountry) { this.originCountry = originCountry; return this; }
        public PricingContext build() { return new PricingContext(this); }
    }
}
