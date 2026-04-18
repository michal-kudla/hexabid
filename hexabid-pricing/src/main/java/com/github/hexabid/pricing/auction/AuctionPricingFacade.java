package com.github.hexabid.pricing.auction;

import com.github.hexabid.pricing.calculator.PercentageCalculator;
import com.github.hexabid.pricing.calculator.PriceCalculator;
import com.github.hexabid.pricing.calculator.SimpleFixedCalculator;
import com.github.hexabid.pricing.component.CompositePriceComponent;
import com.github.hexabid.pricing.component.ComponentResult;
import com.github.hexabid.pricing.component.PricingComponent;
import com.github.hexabid.pricing.component.SimplePriceComponent;
import com.github.hexabid.pricing.model.CustomsDutyRate;
import com.github.hexabid.pricing.model.ExciseRate;
import com.github.hexabid.pricing.model.Money;
import com.github.hexabid.pricing.model.PricingContext;
import com.github.hexabid.pricing.model.VatRate;
import com.github.hexabid.pricing.model.Wadium;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AuctionPricingFacade {

    public AuctionPriceBreakdown calculate(PricingContext context) {
        Money hammerPrice = context.hammerPrice();
        String currency = hammerPrice.currency();

        Money wadiumOffset = context.maybeWadium()
            .map(w -> w.calculate(hammerPrice))
            .orElse(Money.zero(currency));

        Money netto = hammerPrice.subtract(wadiumOffset);

        Money excise = context.isExcisable()
            ? context.maybeExciseRate()
                .map(r -> r.applyTo(netto, context.quantity()))
                .orElse(Money.zero(currency))
            : Money.zero(currency);

        Money customsDuty = context.isImported()
            ? context.maybeCustomsDutyRate()
                .map(r -> r.applyTo(netto.add(excise)))
                .orElse(Money.zero(currency))
            : Money.zero(currency);

        Money vatBase = netto.add(excise).add(customsDuty);
        Money vat = context.maybeVatRate()
            .map(r -> r.applyTo(vatBase))
            .orElse(Money.zero(currency));

        Money totalDue = netto.add(excise).add(customsDuty).add(vat);

        Map<String, Money> details = new LinkedHashMap<>();
        details.put("wadiumPaid", context.maybeWadium().map(Wadium::amountPaid).orElse(Money.zero(currency)));
        details.put("vatBase", vatBase);

        return new AuctionPriceBreakdown(
            hammerPrice, wadiumOffset, netto, excise, customsDuty, vat, totalDue, details);
    }

    public ComponentResult calculateWithComponentTree(PricingContext context) {
        String currency = context.hammerPrice().currency();

        Money wadiumOffset = context.maybeWadium()
            .map(w -> w.calculate(context.hammerPrice()))
            .orElse(Money.zero(currency));
        Money netto = context.hammerPrice().subtract(wadiumOffset);

        SimplePriceComponent hammerComp = SimplePriceComponent.of(
            "hammer-price", new SimpleFixedCalculator("hammer", context.hammerPrice()));

        SimplePriceComponent wadiumComp = SimplePriceComponent.of(
            "wadium-offset", new SimpleFixedCalculator("wadium", wadiumOffset));

        SimplePriceComponent nettoComp = SimplePriceComponent.of(
            "netto", new SimpleFixedCalculator("netto", netto));

        SimplePriceComponent exciseComp = context.isExcisable()
            ? context.maybeExciseRate()
                .map(r -> SimplePriceComponent.builder("excise", createExciseCalculator(r, context))
                    .mapDependent("baseAmount", "netto")
                    .build())
                .orElse(null)
            : null;

        SimplePriceComponent customsComp = context.isImported()
            ? context.maybeCustomsDutyRate()
                .map(r -> SimplePriceComponent.builder("customs-duty",
                        new PercentageCalculator("customs", r.rate()))
                    .mapDependentSum("baseAmount", "netto", "excise")
                    .build())
                .orElse(null)
            : null;

        CompositePriceComponent.Builder totalBuilder = CompositePriceComponent.builder("auction-total")
            .child(hammerComp)
            .child(wadiumComp)
            .child(nettoComp);

        if (exciseComp != null) totalBuilder.child(exciseComp);
        if (customsComp != null) totalBuilder.child(customsComp);

        VatRate vatRate = context.maybeVatRate().orElse(VatRate.ZERO);
        String[] vatDepNames = buildVatDependencyNames(exciseComp != null, customsComp != null);
        SimplePriceComponent vatComp = SimplePriceComponent.builder("vat",
                new PercentageCalculator("vat", vatRate.rate()))
            .mapDependentSum("baseAmount", vatDepNames)
            .build();
        totalBuilder.child(vatComp);

        return totalBuilder.build().calculate(Map.of());
    }

    private PriceCalculator createExciseCalculator(ExciseRate rate, PricingContext context) {
        return new PercentageCalculator("excise", rate.rate());
    }

    private String[] buildVatDependencyNames(boolean hasExcise, boolean hasCustoms) {
        java.util.List<String> names = new java.util.ArrayList<>();
        names.add("netto");
        if (hasExcise) names.add("excise");
        if (hasCustoms) names.add("customs-duty");
        return names.toArray(String[]::new);
    }
}
