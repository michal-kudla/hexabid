package com.github.hexabid.pricing.auction;

import com.github.hexabid.pricing.model.Money;
import java.util.Map;
import java.util.Objects;

public final class AuctionPriceBreakdown {

    private final Money hammerPrice;
    private final Money wadiumOffset;
    private final Money netto;
    private final Money excise;
    private final Money customsDuty;
    private final Money vat;
    private final Money totalDue;
    private final Map<String, Money> details;

    public AuctionPriceBreakdown(Money hammerPrice,
                                  Money wadiumOffset,
                                  Money netto,
                                  Money excise,
                                  Money customsDuty,
                                  Money vat,
                                  Money totalDue,
                                  Map<String, Money> details) {
        this.hammerPrice = Objects.requireNonNull(hammerPrice, "hammerPrice must not be null");
        this.wadiumOffset = Objects.requireNonNull(wadiumOffset, "wadiumOffset must not be null");
        this.netto = Objects.requireNonNull(netto, "netto must not be null");
        this.excise = Objects.requireNonNull(excise, "excise must not be null");
        this.customsDuty = Objects.requireNonNull(customsDuty, "customsDuty must not be null");
        this.vat = Objects.requireNonNull(vat, "vat must not be null");
        this.totalDue = Objects.requireNonNull(totalDue, "totalDue must not be null");
        this.details = Map.copyOf(Objects.requireNonNull(details, "details must not be null"));
    }

    public Money hammerPrice() { return hammerPrice; }
    public Money wadiumOffset() { return wadiumOffset; }
    public Money netto() { return netto; }
    public Money excise() { return excise; }
    public Money customsDuty() { return customsDuty; }
    public Money vat() { return vat; }
    public Money totalDue() { return totalDue; }
    public Map<String, Money> details() { return details; }

    @Override
    public String toString() {
        return "AuctionPriceBreakdown{hammer=%s, wadium=%s, netto=%s, excise=%s, customs=%s, vat=%s, total=%s}"
            .formatted(hammerPrice, wadiumOffset, netto, excise, customsDuty, vat, totalDue);
    }
}
