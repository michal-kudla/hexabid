package com.github.hexabid.pricing.auction;

import com.github.hexabid.pricing.model.CustomsDutyRate;
import com.github.hexabid.pricing.model.ExciseRate;
import com.github.hexabid.pricing.model.Money;
import com.github.hexabid.pricing.model.PricingContext;
import com.github.hexabid.pricing.model.VatRate;
import com.github.hexabid.pricing.model.Wadium;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AuctionPricingFacadeTest {

    private final AuctionPricingFacade facade = new AuctionPricingFacade();

    @Nested
    class E2E1_UniqueCarWithExcise {

        @Test
        void shouldCalculatePriceForDomesticCarWithExcise() {
            PricingContext ctx = PricingContext.builder()
                .hammerPrice(Money.of("50000.00", "PLN"))
                .productType("UNIQUE")
                .excisable(true)
                .imported(false)
                .vatRate(VatRate.TWENTY_THREE)
                .exciseRate(ExciseRate.percentage(new BigDecimal("0.031")))
                .wadium(Wadium.percentage(new BigDecimal("0.05"), Money.of("40000.00", "PLN")))
                .build();

            AuctionPriceBreakdown result = facade.calculate(ctx);

            assertEquals(Money.of("50000.00", "PLN"), result.hammerPrice());
            assertEquals(Money.of("2000.00", "PLN"), result.wadiumOffset());
            assertEquals(Money.of("48000.00", "PLN"), result.netto());
            assertEquals(Money.of("1488.00", "PLN"), result.excise());
            assertEquals(Money.of("0.00", "PLN"), result.customsDuty());
            assertEquals(Money.of("11382.24", "PLN"), result.vat());
            assertEquals(Money.of("60870.24", "PLN"), result.totalDue());
        }
    }

    @Nested
    class E2E2_ImportedFlourWithCustoms {

        @Test
        void shouldCalculatePriceForImportedFlourWithCustomsDuty() {
            PricingContext ctx = PricingContext.builder()
                .hammerPrice(Money.of("300.00", "PLN"))
                .quantity(new BigDecimal("100"))
                .productType("IDENTICAL")
                .excisable(false)
                .imported(true)
                .vatRate(VatRate.FIVE)
                .customsDutyRate(CustomsDutyRate.of(new BigDecimal("0.05")))
                .wadium(Wadium.fixed(Money.of("50.00", "PLN")))
                .build();

            AuctionPriceBreakdown result = facade.calculate(ctx);

            assertEquals(Money.of("300.00", "PLN"), result.hammerPrice());
            assertEquals(Money.of("50.00", "PLN"), result.wadiumOffset());
            assertEquals(Money.of("250.00", "PLN"), result.netto());
            assertEquals(Money.of("0.00", "PLN"), result.excise());
            assertEquals(Money.of("12.50", "PLN"), result.customsDuty());
            assertEquals(Money.of("13.13", "PLN"), result.vat());
            assertEquals(Money.of("275.63", "PLN"), result.totalDue());
        }
    }

    @Nested
    class E2E3_ImportedPhoneNoExciseNoCustoms {

        @Test
        void shouldCalculatePriceForPhoneWithZeroCustomsInEU() {
            PricingContext ctx = PricingContext.builder()
                .hammerPrice(Money.of("6000.00", "PLN"))
                .productType("INDIVIDUALLY_TRACKED")
                .excisable(false)
                .imported(true)
                .vatRate(VatRate.TWENTY_THREE)
                .customsDutyRate(CustomsDutyRate.of(new BigDecimal("0.00")))
                .wadium(Wadium.percentage(new BigDecimal("0.10"), Money.of("5000.00", "PLN")))
                .build();

            AuctionPriceBreakdown result = facade.calculate(ctx);

            assertEquals(Money.of("6000.00", "PLN"), result.hammerPrice());
            assertEquals(Money.of("500.00", "PLN"), result.wadiumOffset());
            assertEquals(Money.of("5500.00", "PLN"), result.netto());
            assertEquals(Money.of("0.00", "PLN"), result.excise());
            assertEquals(Money.of("0.00", "PLN"), result.customsDuty());
            assertEquals(Money.of("1265.00", "PLN"), result.vat());
            assertEquals(Money.of("6765.00", "PLN"), result.totalDue());
        }
    }

    @Nested
    class E2E4_AlcoholWithPerUnitExcise {

        @Test
        void shouldCalculatePriceForAlcoholWithPerUnitExcise() {
            PricingContext ctx = PricingContext.builder()
                .hammerPrice(Money.of("2000.00", "PLN"))
                .quantity(new BigDecimal("50"))
                .productType("BATCH_TRACKED")
                .excisable(true)
                .imported(false)
                .vatRate(VatRate.TWENTY_THREE)
                .exciseRate(ExciseRate.perUnit(new BigDecimal("1.17")))
                .wadium(Wadium.percentage(new BigDecimal("0.05"), Money.of("1800.00", "PLN")))
                .build();

            AuctionPriceBreakdown result = facade.calculate(ctx);

            assertEquals(Money.of("2000.00", "PLN"), result.hammerPrice());
            assertEquals(Money.of("90.00", "PLN"), result.wadiumOffset());
            assertEquals(Money.of("1910.00", "PLN"), result.netto());
            assertEquals(Money.of("58.50", "PLN"), result.excise());
            assertEquals(Money.of("0.00", "PLN"), result.customsDuty());
            assertEquals(Money.of("452.76", "PLN"), result.vat());
            assertEquals(Money.of("2421.26", "PLN"), result.totalDue());
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void shouldCalculateWithoutWadium() {
            PricingContext ctx = PricingContext.builder()
                .hammerPrice(Money.of("100.00", "PLN"))
                .vatRate(VatRate.TWENTY_THREE)
                .build();

            AuctionPriceBreakdown result = facade.calculate(ctx);

            assertEquals(Money.of("100.00", "PLN"), result.hammerPrice());
            assertEquals(Money.of("0.00", "PLN"), result.wadiumOffset());
            assertEquals(Money.of("100.00", "PLN"), result.netto());
            assertEquals(Money.of("23.00", "PLN"), result.vat());
            assertEquals(Money.of("123.00", "PLN"), result.totalDue());
        }

        @Test
        void shouldCalculateWithoutVatRate() {
            PricingContext ctx = PricingContext.builder()
                .hammerPrice(Money.of("100.00", "PLN"))
                .build();

            AuctionPriceBreakdown result = facade.calculate(ctx);

            assertEquals(Money.of("100.00", "PLN"), result.netto());
            assertEquals(Money.of("0.00", "PLN"), result.vat());
            assertEquals(Money.of("100.00", "PLN"), result.totalDue());
        }

        @Test
        void shouldNotApplyExciseForNonExcisableProduct() {
            PricingContext ctx = PricingContext.builder()
                .hammerPrice(Money.of("1000.00", "PLN"))
                .excisable(false)
                .vatRate(VatRate.TWENTY_THREE)
                .build();

            AuctionPriceBreakdown result = facade.calculate(ctx);

            assertEquals(Money.of("0.00", "PLN"), result.excise());
        }

        @Test
        void shouldNotApplyCustomsForDomesticProduct() {
            PricingContext ctx = PricingContext.builder()
                .hammerPrice(Money.of("1000.00", "PLN"))
                .imported(false)
                .vatRate(VatRate.TWENTY_THREE)
                .build();

            AuctionPriceBreakdown result = facade.calculate(ctx);

            assertEquals(Money.of("0.00", "PLN"), result.customsDuty());
        }

        @Test
        void shouldApplyCustomsOnNettoPlusExcise() {
            PricingContext ctx = PricingContext.builder()
                .hammerPrice(Money.of("1000.00", "PLN"))
                .excisable(true)
                .imported(true)
                .vatRate(VatRate.TWENTY_THREE)
                .exciseRate(ExciseRate.percentage(new BigDecimal("0.10")))
                .customsDutyRate(CustomsDutyRate.of(new BigDecimal("0.05")))
                .build();

            AuctionPriceBreakdown result = facade.calculate(ctx);

            assertEquals(Money.of("1000.00", "PLN"), result.netto());
            assertEquals(Money.of("100.00", "PLN"), result.excise());
            assertEquals(Money.of("55.00", "PLN"), result.customsDuty());
            assertEquals(Money.of("265.65", "PLN"), result.vat());
        }
    }

    @Nested
    class ComponentTreeTests {

        @Test
        void shouldProduceFullBreakdownTreeForCarAuction() {
            PricingContext ctx = PricingContext.builder()
                .hammerPrice(Money.of("50000.00", "PLN"))
                .productType("UNIQUE")
                .excisable(true)
                .imported(false)
                .vatRate(VatRate.TWENTY_THREE)
                .exciseRate(ExciseRate.percentage(new BigDecimal("0.031")))
                .wadium(Wadium.percentage(new BigDecimal("0.05"), Money.of("40000.00", "PLN")))
                .build();

            var result = facade.calculateWithComponentTree(ctx);

            assertEquals("auction-total", result.name());
            assertTrue(result.children().containsKey("netto"));
            assertTrue(result.children().containsKey("excise"));
            assertTrue(result.children().containsKey("vat"));
            assertFalse(result.children().containsKey("customs-duty"));
            assertEquals(Money.of("48000.00", "PLN"), result.children().get("netto").value());
            assertEquals(Money.of("1488.00", "PLN"), result.children().get("excise").value());
            assertEquals(Money.of("11382.24", "PLN"), result.children().get("vat").value());
        }

        @Test
        void shouldIncludeCustomsInTreeForImportedProduct() {
            PricingContext ctx = PricingContext.builder()
                .hammerPrice(Money.of("1000.00", "PLN"))
                .excisable(false)
                .imported(true)
                .vatRate(VatRate.TWENTY_THREE)
                .customsDutyRate(CustomsDutyRate.of(new BigDecimal("0.05")))
                .build();

            var result = facade.calculateWithComponentTree(ctx);

            assertTrue(result.children().containsKey("customs-duty"));
            assertFalse(result.children().containsKey("excise"));
        }
    }
}
