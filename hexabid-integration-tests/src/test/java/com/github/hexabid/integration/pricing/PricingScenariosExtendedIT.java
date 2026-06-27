package com.github.hexabid.integration.pricing;

import com.github.hexabid.contract.client.ApiClient;
import com.github.hexabid.contract.client.api.AuctionsApi;
import com.github.hexabid.contract.client.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PricingScenariosExtendedIT {

    private static final String BASE_URL = "http://localhost:18080/hexabid";
    private static final String API_VERSION = "1";
    private static final String SELLER_USER = "user";
    private static final String SELLER_PASS = "password";
    private static final String BUYER_USER = "admin";
    private static final String BUYER_PASS = "password";

    private static AuctionsApi auctionsApi;
    private static AuctionsApi buyerAuctionsApi;

    @BeforeAll
    static void setupApiClient() {
        ApiClient sellerClient = new ApiClient();
        sellerClient.updateBaseUri(BASE_URL);
        sellerClient.setRequestInterceptor(builder -> {
            builder.header("X-API-Version", API_VERSION);
            builder.header("Authorization", basicAuth(SELLER_USER, SELLER_PASS));
        });
        auctionsApi = new AuctionsApi(sellerClient);

        ApiClient buyerClient = new ApiClient();
        buyerClient.updateBaseUri(BASE_URL);
        buyerClient.setRequestInterceptor(builder -> {
            builder.header("X-API-Version", API_VERSION);
            builder.header("Authorization", basicAuth(BUYER_USER, BUYER_PASS));
        });
        buyerAuctionsApi = new AuctionsApi(buyerClient);
    }

    private static String basicAuth(String user, String pass) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
    }

    private static Money pln(String amount) {
        Money m = new Money();
        m.setAmount(amount);
        m.setCurrency("PLN");
        return m;
    }

    private CreateAuctionRequest createAuctionWith(String title, String startingPrice, PricingConfig config) {
        CreateAuctionRequest req = new CreateAuctionRequest();
        req.setTitle(title + " " + UUID.randomUUID());
        req.setStartingPrice(pln(startingPrice));
        req.setEndsAt(OffsetDateTime.now().plusHours(4));
        req.setPricingConfig(config);
        return req;
    }

    private PricingConfig pricingConfigImportedExcisableCar() {
        PricingConfig c = new PricingConfig();
        c.setWadiumStrategy(WadiumStrategy.PERCENTAGE);
        c.setWadiumRate("0.10");
        c.setVatRate("0.23");
        c.setIsExcisable(true);
        c.setExciseRate("0.18");
        c.setExciseType(PricingConfig.ExciseTypeEnum.PERCENTAGE);
        c.setIsImported(true);
        c.setCustomsDutyRate("0.10");
        return c;
    }

    private PricingConfig pricingConfigPerUnitExcise() {
        PricingConfig c = new PricingConfig();
        c.setWadiumStrategy(WadiumStrategy.FIXED);
        c.setWadiumFixedAmount(pln("200.00"));
        c.setVatRate("0.23");
        c.setIsExcisable(true);
        c.setExciseRate("1.17");
        c.setExciseType(PricingConfig.ExciseTypeEnum.PER_UNIT);
        c.setIsImported(false);
        return c;
    }

    private PricingConfig pricingConfigFixedWadium() {
        PricingConfig c = new PricingConfig();
        c.setWadiumStrategy(WadiumStrategy.FIXED);
        c.setWadiumFixedAmount(pln("1000.00"));
        c.setVatRate("0.23");
        c.setIsExcisable(false);
        c.setIsImported(false);
        return c;
    }

    private PricingConfig pricingConfigZeroVat() {
        PricingConfig c = new PricingConfig();
        c.setVatRate("0.00");
        c.setIsExcisable(false);
        c.setIsImported(false);
        return c;
    }

    private PricingConfig pricingConfigReducedVat8() {
        PricingConfig c = new PricingConfig();
        c.setVatRate("0.08");
        c.setIsExcisable(false);
        c.setIsImported(false);
        return c;
    }

    private PricingConfig pricingConfigReducedVat5() {
        PricingConfig c = new PricingConfig();
        c.setVatRate("0.05");
        c.setIsExcisable(false);
        c.setIsImported(false);
        return c;
    }

    private PricingConfig pricingConfigVatOnly(String vatRate) {
        PricingConfig c = new PricingConfig();
        c.setVatRate(vatRate);
        c.setIsExcisable(false);
        c.setIsImported(false);
        return c;
    }

    private PricingConfig pricingConfigCar() {
        PricingConfig c = new PricingConfig();
        c.setWadiumStrategy(WadiumStrategy.PERCENTAGE);
        c.setWadiumRate("0.05");
        c.setVatRate("0.23");
        c.setIsExcisable(true);
        c.setExciseRate("0.031");
        c.setExciseType(PricingConfig.ExciseTypeEnum.PERCENTAGE);
        c.setIsImported(false);
        return c;
    }

    private PricingConfig pricingConfigImportedFlour() {
        PricingConfig c = new PricingConfig();
        c.setWadiumStrategy(WadiumStrategy.FIXED);
        c.setWadiumFixedAmount(pln("50.00"));
        c.setVatRate("0.05");
        c.setIsExcisable(false);
        c.setIsImported(true);
        c.setCustomsDutyRate("0.05");
        return c;
    }

    @Nested
    class IT_P10_ImportedExcisableCarFullTax {
        @Test
        void shouldCalculateFullBreakdownForImportedExcisableCar() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Imported excisable car", "100000.00", pricingConfigImportedExcisableCar()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            BigDecimal hammer = new BigDecimal(price.getHammerPrice().getAmount());
            BigDecimal wadiumOffset = new BigDecimal(price.getWadiumOffset().getAmount());
            BigDecimal netto = new BigDecimal(price.getNetto().getAmount());
            BigDecimal excise = new BigDecimal(price.getExcise().getAmount());
            BigDecimal customs = new BigDecimal(price.getCustomsDuty().getAmount());
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            BigDecimal totalDue = new BigDecimal(price.getTotalDue().getAmount());

            assertThat(wadiumOffset).isEqualByComparingTo(hammer.multiply(new BigDecimal("0.10"))
                .setScale(2, RoundingMode.HALF_UP));
            assertThat(netto).isEqualByComparingTo(hammer.subtract(wadiumOffset));
            assertThat(excise).isEqualByComparingTo(netto.multiply(new BigDecimal("0.18"))
                .setScale(2, RoundingMode.HALF_UP));
            assertThat(customs).isEqualByComparingTo(netto.add(excise).multiply(new BigDecimal("0.10"))
                .setScale(2, RoundingMode.HALF_UP));

            BigDecimal vatBase = netto.add(excise).add(customs);
            assertThat(vat).isEqualByComparingTo(vatBase.multiply(new BigDecimal("0.23"))
                .setScale(2, RoundingMode.HALF_UP));
            assertThat(totalDue).isEqualByComparingTo(netto.add(excise).add(customs).add(vat));

            assertThat(price.getAppliedRates().getVatRate()).isEqualTo("23%");
            assertThat(price.getAppliedRates().getExciseRate()).isEqualTo("18%");
            assertThat(price.getAppliedRates().getCustomsDutyRate()).isEqualTo("10%");
            assertThat(price.getAppliedRates().getWadiumType()).isEqualTo(AppliedRates.WadiumTypeEnum.PERCENTAGE);
        }
    }

    @Nested
    class IT_P11_PerUnitExciseFuel {
        @Test
        void shouldCalculatePerUnitExciseForFuelProduct() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Fuel diesel 1000L", "5000.00", pricingConfigPerUnitExcise()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getHammerPrice().getAmount()).isEqualTo("5000.00");
            assertThat(price.getWadiumOffset().getAmount()).isEqualTo("200.00");
            assertThat(price.getNetto().getAmount()).isEqualTo("4800.00");
            assertThat(new BigDecimal(price.getExcise().getAmount())).isGreaterThan(BigDecimal.ZERO);
            assertThat(price.getCustomsDuty().getAmount()).isEqualTo("0.00");

            BigDecimal netto = new BigDecimal(price.getNetto().getAmount());
            BigDecimal excise = new BigDecimal(price.getExcise().getAmount());
            BigDecimal vatBase = netto.add(excise);
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            assertThat(vat).isEqualByComparingTo(vatBase.multiply(new BigDecimal("0.23"))
                .setScale(2, RoundingMode.HALF_UP));

            assertThat(price.getAppliedRates().getWadiumType()).isEqualTo(AppliedRates.WadiumTypeEnum.FIXED);
        }
    }

    @Nested
    class IT_P12_FixedWadiumDeduction {
        @Test
        void shouldDeductFixedWadiumFromHammerPrice() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Fixed wadium item", "10000.00", pricingConfigFixedWadium()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getHammerPrice().getAmount()).isEqualTo("10000.00");
            assertThat(price.getWadiumOffset().getAmount()).isEqualTo("1000.00");
            assertThat(price.getNetto().getAmount()).isEqualTo("9000.00");

            BigDecimal netto = new BigDecimal("9000.00");
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            assertThat(vat).isEqualByComparingTo(netto.multiply(new BigDecimal("0.23"))
                .setScale(2, RoundingMode.HALF_UP));

            assertThat(price.getAppliedRates().getWadiumType()).isEqualTo(AppliedRates.WadiumTypeEnum.FIXED);
        }
    }

    @Nested
    class IT_P13_ZeroVatExemptProduct {
        @Test
        void shouldReturnZeroVatForExemptProduct() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Tax-exempt medical", "2000.00", pricingConfigZeroVat()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getHammerPrice().getAmount()).isEqualTo("2000.00");
            assertThat(price.getWadiumOffset().getAmount()).isEqualTo("0.00");
            assertThat(price.getNetto().getAmount()).isEqualTo("2000.00");
            assertThat(price.getVat().getAmount()).isEqualTo("0.00");
            assertThat(price.getExcise().getAmount()).isEqualTo("0.00");
            assertThat(price.getCustomsDuty().getAmount()).isEqualTo("0.00");

            BigDecimal totalDue = new BigDecimal(price.getTotalDue().getAmount());
            assertThat(totalDue).isEqualByComparingTo(new BigDecimal("2000.00"));
        }
    }

    @Nested
    class IT_P14_ReducedVat8Percent {
        @Test
        void shouldApplyReduced8PercentVat() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Reduced VAT 8pct", "1000.00", pricingConfigReducedVat8()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getNetto().getAmount()).isEqualTo("1000.00");

            BigDecimal netto = new BigDecimal("1000.00");
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            assertThat(vat).isEqualByComparingTo(netto.multiply(new BigDecimal("0.08"))
                .setScale(2, RoundingMode.HALF_UP));
            assertThat(price.getAppliedRates().getVatRate()).isEqualTo("8%");
        }
    }

    @Nested
    class IT_P15_ReducedVat5Percent {
        @Test
        void shouldApplyReduced5PercentVat() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Reduced VAT 5pct", "500.00", pricingConfigReducedVat5()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getNetto().getAmount()).isEqualTo("500.00");

            BigDecimal netto = new BigDecimal("500.00");
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            assertThat(vat).isEqualByComparingTo(netto.multiply(new BigDecimal("0.05"))
                .setScale(2, RoundingMode.HALF_UP));
            assertThat(price.getAppliedRates().getVatRate()).isEqualTo("5%");
        }
    }

    @Nested
    class IT_P16_TotalDueEqualsSumOfComponents {
        @Test
        void shouldVerifyTotalDueIsSumOfNettoExciseCustomsVat() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Sum verification", "100000.00", pricingConfigImportedExcisableCar()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            BigDecimal netto = new BigDecimal(price.getNetto().getAmount());
            BigDecimal excise = new BigDecimal(price.getExcise().getAmount());
            BigDecimal customs = new BigDecimal(price.getCustomsDuty().getAmount());
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            BigDecimal totalDue = new BigDecimal(price.getTotalDue().getAmount());

            assertThat(totalDue).isEqualByComparingTo(netto.add(excise).add(customs).add(vat));
        }
    }

    @Nested
    class IT_P17_HammerEqualsNettoPlusWadium {
        @Test
        void shouldVerifyHammerPriceEqualsNettoPlusWadiumOffset() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Hammer=netto+wadium", "50000.00", pricingConfigCar()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            BigDecimal hammer = new BigDecimal(price.getHammerPrice().getAmount());
            BigDecimal wadium = new BigDecimal(price.getWadiumOffset().getAmount());
            BigDecimal netto = new BigDecimal(price.getNetto().getAmount());

            assertThat(hammer).isEqualByComparingTo(netto.add(wadium));
        }
    }

    @Nested
    class IT_P18_SmallAmountPriceBreakdown {
        @Test
        void shouldCalculateCorrectlyForVerySmallAmount() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Small amount", "1.00", pricingConfigVatOnly("0.23")), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getHammerPrice().getAmount()).isEqualTo("1.00");
            assertThat(price.getNetto().getAmount()).isEqualTo("1.00");
            assertThat(new BigDecimal(price.getVat().getAmount())).isEqualByComparingTo(new BigDecimal("0.23"));
        }
    }

    @Nested
    class IT_P19_LargeAmountPriceBreakdown {
        @Test
        void shouldCalculateCorrectlyForLargeAmount() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Large amount", "9999999.99", pricingConfigVatOnly("0.23")), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            BigDecimal hammer = new BigDecimal("9999999.99");
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            assertThat(vat).isEqualByComparingTo(hammer.multiply(new BigDecimal("0.23"))
                .setScale(2, RoundingMode.HALF_UP));

            BigDecimal totalDue = new BigDecimal(price.getTotalDue().getAmount());
            assertThat(totalDue).isEqualByComparingTo(hammer.add(vat));
        }
    }

    @Nested
    class IT_P20_WadiumDepositAndPriceVerification {
        @Test
        void shouldDepositWadiumAndVerifyPriceReflectsIt() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Wadium+price", "10000.00", pricingConfigCar()), API_VERSION);

            AuctionPriceBreakdownResponse priceBefore = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);
            assertThat(priceBefore.getWadiumOffset().getAmount()).isNotBlank();

            DepositWadiumRequest wadiumReq = new DepositWadiumRequest();
            wadiumReq.setAmount(pln("500.00"));
            WadiumResponse wadium = buyerAuctionsApi.depositWadium(
                auction.getAuctionId(), wadiumReq, API_VERSION);

            assertThat(wadium.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);
            assertThat(wadium.getRefundableOnLoss()).isTrue();
            assertThat(wadium.getDeductibleOnWin()).isTrue();
            assertThat(wadium.getAuctionId()).isEqualTo(auction.getAuctionId());
        }
    }

    @Nested
    class IT_P21_MultiplePriceBreakdownCallsConsistent {
        @Test
        void shouldReturnSameResultOnRepeatedPriceQueries() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Consistency check", "50000.00", pricingConfigCar()), API_VERSION);

            AuctionPriceBreakdownResponse first = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);
            AuctionPriceBreakdownResponse second = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(first.getHammerPrice().getAmount()).isEqualTo(second.getHammerPrice().getAmount());
            assertThat(first.getNetto().getAmount()).isEqualTo(second.getNetto().getAmount());
            assertThat(first.getVat().getAmount()).isEqualTo(second.getVat().getAmount());
            assertThat(first.getTotalDue().getAmount()).isEqualTo(second.getTotalDue().getAmount());
        }
    }

    @Nested
    class IT_P22_ImportedProductWithHighCustomsDuty {
        @Test
        void shouldApplyHighCustomsDutyForImportedGoods() throws Exception {
            PricingConfig c = new PricingConfig();
            c.setVatRate("0.23");
            c.setIsExcisable(false);
            c.setIsImported(true);
            c.setCustomsDutyRate("0.25");

            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("High customs", "4000.00", c), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            BigDecimal netto = new BigDecimal(price.getNetto().getAmount());
            BigDecimal customs = new BigDecimal(price.getCustomsDuty().getAmount());
            assertThat(customs).isEqualByComparingTo(netto.multiply(new BigDecimal("0.25"))
                .setScale(2, RoundingMode.HALF_UP));

            BigDecimal vatBase = netto.add(customs);
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            assertThat(vat).isEqualByComparingTo(vatBase.multiply(new BigDecimal("0.23"))
                .setScale(2, RoundingMode.HALF_UP));
        }
    }

    @Nested
    class IT_P23_VatBaseIncludesExciseAndCustoms {
        @Test
        void shouldCalculateVatOnNettoPlusExcisePlusCustoms() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("VAT base check", "100000.00", pricingConfigImportedExcisableCar()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            BigDecimal netto = new BigDecimal(price.getNetto().getAmount());
            BigDecimal excise = new BigDecimal(price.getExcise().getAmount());
            BigDecimal customs = new BigDecimal(price.getCustomsDuty().getAmount());
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());

            BigDecimal expectedVatBase = netto.add(excise).add(customs);
            BigDecimal expectedVat = expectedVatBase.multiply(new BigDecimal("0.23"))
                .setScale(2, RoundingMode.HALF_UP);
            assertThat(vat).isEqualByComparingTo(expectedVat);
        }
    }

    @Nested
    class IT_P24_AppliedRatesReflectPricingConfig {
        @Test
        void shouldReturnAppliedRatesMatchingConfiguration() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Rates reflection", "50000.00", pricingConfigCar()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getAppliedRates()).isNotNull();
            assertThat(price.getAppliedRates().getVatRate()).isNotBlank();
            assertThat(price.getAppliedRates().getWadiumType()).isEqualTo(AppliedRates.WadiumTypeEnum.PERCENTAGE);
        }
    }

    @Nested
    class IT_P25_ImportedFlourFullBreakdown {
        @Test
        void shouldCalculateImportedFlourWithFixedWadiumAndCustoms() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Flour full", "300.00", pricingConfigImportedFlour()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getHammerPrice().getAmount()).isEqualTo("300.00");
            assertThat(price.getWadiumOffset().getAmount()).isEqualTo("50.00");
            assertThat(price.getNetto().getAmount()).isEqualTo("250.00");
            assertThat(price.getExcise().getAmount()).isEqualTo("0.00");

            BigDecimal netto = new BigDecimal("250.00");
            BigDecimal expectedCustoms = netto.multiply(new BigDecimal("0.05"))
                .setScale(2, RoundingMode.HALF_UP);
            assertThat(new BigDecimal(price.getCustomsDuty().getAmount())).isEqualByComparingTo(expectedCustoms);

            BigDecimal customs = new BigDecimal(price.getCustomsDuty().getAmount());
            BigDecimal vatBase = netto.add(customs);
            BigDecimal expectedVat = vatBase.multiply(new BigDecimal("0.05"))
                .setScale(2, RoundingMode.HALF_UP);
            assertThat(new BigDecimal(price.getVat().getAmount())).isEqualByComparingTo(expectedVat);

            assertThat(price.getAppliedRates().getVatRate()).isEqualTo("5%");
            assertThat(price.getAppliedRates().getCustomsDutyRate()).isEqualTo("5%");
            assertThat(price.getAppliedRates().getWadiumType()).isEqualTo(AppliedRates.WadiumTypeEnum.FIXED);
        }
    }

    @Nested
    class IT_P26_WadiumRefundThenRedeposit {
        @Test
        void shouldRefundAndAllowNewWadiumDeposit() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Refund redeposit", "10000.00", pricingConfigCar()), API_VERSION);

            DepositWadiumRequest wadiumReq = new DepositWadiumRequest();
            wadiumReq.setAmount(pln("500.00"));
            WadiumResponse deposit = buyerAuctionsApi.depositWadium(
                auction.getAuctionId(), wadiumReq, API_VERSION);
            assertThat(deposit.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);

            RefundWadiumRequest refundReq = new RefundWadiumRequest();
            refundReq.setPartyId(auction.getAuctionId());
            WadiumRefundResponse refund = buyerAuctionsApi.refundWadium(
                auction.getAuctionId(), refundReq, API_VERSION);
            assertThat(refund.getStatus()).isEqualTo(WadiumRefundResponse.StatusEnum.REFUNDED);
            assertThat(refund.getRefundAmount().getAmount()).isEqualTo("500.00");
        }
    }

    @Nested
    class IT_P27_PriceBreakdownForAuctionWithoutPricingConfig {
        @Test
        void shouldReturnPriceBreakdownEvenWithoutPricingConfig() throws Exception {
            CreateAuctionRequest req = new CreateAuctionRequest();
            req.setTitle("No pricing config " + UUID.randomUUID());
            req.setStartingPrice(pln("5000.00"));
            req.setEndsAt(OffsetDateTime.now().plusHours(4));

            AuctionResponse auction = auctionsApi.createAuction(req, API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price).isNotNull();
            assertThat(price.getHammerPrice().getAmount()).isEqualTo("5000.00");
            assertThat(price.getTotalDue()).isNotNull();
        }
    }

    @Nested
    class IT_P28_ExciseOnlyNoCustomsNoWadium {
        @Test
        void shouldCalculateExciseWithoutCustomsOrWadium() throws Exception {
            PricingConfig c = new PricingConfig();
            c.setVatRate("0.23");
            c.setIsExcisable(true);
            c.setExciseRate("0.031");
            c.setExciseType(PricingConfig.ExciseTypeEnum.PERCENTAGE);
            c.setIsImported(false);

            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Excise only", "50000.00", c), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getHammerPrice().getAmount()).isEqualTo("50000.00");
            assertThat(price.getWadiumOffset().getAmount()).isEqualTo("0.00");
            assertThat(price.getNetto().getAmount()).isEqualTo("50000.00");
            assertThat(new BigDecimal(price.getExcise().getAmount())).isGreaterThan(BigDecimal.ZERO);
            assertThat(price.getCustomsDuty().getAmount()).isEqualTo("0.00");

            BigDecimal netto = new BigDecimal("50000.00");
            BigDecimal excise = new BigDecimal(price.getExcise().getAmount());
            assertThat(excise).isEqualByComparingTo(netto.multiply(new BigDecimal("0.031"))
                .setScale(2, RoundingMode.HALF_UP));
        }
    }

    @Nested
    class IT_P29_CustomsOnlyNoExciseNoWadium {
        @Test
        void shouldCalculateCustomsWithoutExciseOrWadium() throws Exception {
            PricingConfig c = new PricingConfig();
            c.setVatRate("0.23");
            c.setIsExcisable(false);
            c.setIsImported(true);
            c.setCustomsDutyRate("0.12");

            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Customs only", "10000.00", c), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getNetto().getAmount()).isEqualTo("10000.00");
            assertThat(price.getExcise().getAmount()).isEqualTo("0.00");
            assertThat(new BigDecimal(price.getCustomsDuty().getAmount())).isGreaterThan(BigDecimal.ZERO);

            BigDecimal netto = new BigDecimal("10000.00");
            BigDecimal customs = new BigDecimal(price.getCustomsDuty().getAmount());
            assertThat(customs).isEqualByComparingTo(netto.multiply(new BigDecimal("0.12"))
                .setScale(2, RoundingMode.HALF_UP));

            BigDecimal vatBase = netto.add(customs);
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            assertThat(vat).isEqualByComparingTo(vatBase.multiply(new BigDecimal("0.23"))
                .setScale(2, RoundingMode.HALF_UP));
        }
    }

    @Nested
    class IT_P30_PercentageWadiumRateApplied {
        @Test
        void shouldApplyPercentageWadiumRateToHammerPrice() throws Exception {
            PricingConfig c = new PricingConfig();
            c.setWadiumStrategy(WadiumStrategy.PERCENTAGE);
            c.setWadiumRate("0.05");
            c.setVatRate("0.23");
            c.setIsExcisable(false);
            c.setIsImported(false);

            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Pct wadium rate", "20000.00", c), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            BigDecimal hammer = new BigDecimal("20000.00");
            BigDecimal wadiumOffset = new BigDecimal(price.getWadiumOffset().getAmount());
            assertThat(wadiumOffset).isEqualByComparingTo(hammer.multiply(new BigDecimal("0.05"))
                .setScale(2, RoundingMode.HALF_UP));

            assertThat(price.getAppliedRates().getWadiumType()).isEqualTo(AppliedRates.WadiumTypeEnum.PERCENTAGE);
        }
    }

    @Nested
    class IT_P31_FullFlowCreateBidDepositPriceRefund {
        @Test
        void shouldCompleteFullPricingLifecycleWithWadium() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Full lifecycle", "50000.00", pricingConfigCar()), API_VERSION);
            assertThat(auction.getPricingConfig()).isNotNull();

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);
            assertThat(price.getHammerPrice().getAmount()).isEqualTo("50000.00");

            DepositWadiumRequest wadiumReq = new DepositWadiumRequest();
            wadiumReq.setAmount(pln("2500.00"));
            WadiumResponse wadium = buyerAuctionsApi.depositWadium(
                auction.getAuctionId(), wadiumReq, API_VERSION);
            assertThat(wadium.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);
            assertThat(wadium.getDeductibleOnWin()).isTrue();
            assertThat(wadium.getRefundableOnLoss()).isTrue();

            AuctionPriceBreakdownResponse priceAfterDeposit = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);
            assertThat(priceAfterDeposit.getTotalDue().getAmount()).isNotBlank();

            BigDecimal netto = new BigDecimal(priceAfterDeposit.getNetto().getAmount());
            BigDecimal excise = new BigDecimal(priceAfterDeposit.getExcise().getAmount());
            BigDecimal customs = new BigDecimal(priceAfterDeposit.getCustomsDuty().getAmount());
            BigDecimal vat = new BigDecimal(priceAfterDeposit.getVat().getAmount());
            BigDecimal totalDue = new BigDecimal(priceAfterDeposit.getTotalDue().getAmount());
            assertThat(totalDue).isEqualByComparingTo(netto.add(excise).add(customs).add(vat));

            RefundWadiumRequest refundReq = new RefundWadiumRequest();
            refundReq.setPartyId(auction.getAuctionId());
            WadiumRefundResponse refund = buyerAuctionsApi.refundWadium(
                auction.getAuctionId(), refundReq, API_VERSION);
            assertThat(refund.getStatus()).isEqualTo(WadiumRefundResponse.StatusEnum.REFUNDED);
            assertThat(refund.getRefundAmount().getAmount()).isEqualTo("2500.00");
        }
    }
}
