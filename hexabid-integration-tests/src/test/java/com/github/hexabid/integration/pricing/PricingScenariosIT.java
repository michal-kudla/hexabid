package com.github.hexabid.integration.pricing;

import com.github.hexabid.contract.client.ApiClient;
import com.github.hexabid.contract.client.api.AuctionsApi;
import com.github.hexabid.contract.client.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PricingScenariosIT {

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

    private PricingConfig pricingConfigVatOnly(String vatRate) {
        PricingConfig c = new PricingConfig();
        c.setVatRate(vatRate);
        c.setIsExcisable(false);
        c.setIsImported(false);
        return c;
    }

    @Nested
    class IT_P1_CreateAuctionWithPricingConfig {
        @Test
        void shouldCreateAuctionWithPricingConfigAndReturnIt() throws Exception {
            CreateAuctionRequest req = createAuctionWith("Pricing auction", "50000.00", pricingConfigCar());

            AuctionResponse response = auctionsApi.createAuction(req, API_VERSION);

            assertThat(response).isNotNull();
            assertThat(response.getAuctionId()).isNotNull();
            assertThat(response.getPricingConfig()).isNotNull();
            assertThat(response.getPricingConfig().getWadiumStrategy()).isEqualTo(WadiumStrategy.PERCENTAGE);
            assertThat(response.getPricingConfig().getVatRate()).isEqualTo("0.23");
            assertThat(response.getPricingConfig().getIsExcisable()).isTrue();
        }
    }

    @Nested
    class IT_P2_DepositWadium {
        @Test
        void shouldDepositWadiumForAuction() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Wadium deposit", "10000.00", pricingConfigCar()), API_VERSION);

            DepositWadiumRequest wadiumReq = new DepositWadiumRequest();
            wadiumReq.setAmount(pln("500.00"));

            WadiumResponse wadiumResponse = buyerAuctionsApi.depositWadium(
                auction.getAuctionId(), wadiumReq, API_VERSION);

            assertThat(wadiumResponse.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);
            assertThat(wadiumResponse.getRefundableOnLoss()).isTrue();
            assertThat(wadiumResponse.getDeductibleOnWin()).isTrue();
            assertThat(wadiumResponse.getAmount().getAmount()).isEqualTo("500.00");
        }
    }

    @Nested
    class IT_P3_PriceBreakdownCarWithExcise {
        @Test
        void shouldReturnPriceBreakdownForDomesticCar() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Car excise", "50000.00", pricingConfigCar()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getHammerPrice().getAmount()).isEqualTo("50000.00");
            assertThat(price.getWadiumOffset().getAmount()).isEqualTo("2500.00");
            assertThat(price.getNetto().getAmount()).isEqualTo("47500.00");
            assertThat(price.getExcise().getAmount()).isEqualTo("1472.50");
            assertThat(price.getCustomsDuty().getAmount()).isEqualTo("0.00");
            assertThat(price.getVat().getAmount()).isEqualTo("11263.68");
            assertThat(price.getTotalDue().getAmount()).isEqualTo("60236.18");
            assertThat(price.getAppliedRates().getVatRate()).isEqualTo("23%");
            assertThat(price.getAppliedRates().getExciseRate()).isEqualTo("3.1%");
        }
    }

    @Nested
    class IT_P4_PriceBreakdownImportedFlourWithCustoms {
        @Test
        void shouldReturnPriceBreakdownForImportedFlour() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Flour import", "300.00", pricingConfigImportedFlour()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getHammerPrice().getAmount()).isEqualTo("300.00");
            assertThat(price.getWadiumOffset().getAmount()).isEqualTo("50.00");
            assertThat(price.getNetto().getAmount()).isEqualTo("250.00");
            assertThat(price.getExcise().getAmount()).isEqualTo("0.00");
            assertThat(price.getCustomsDuty().getAmount()).isEqualTo("12.50");
            assertThat(price.getVat().getAmount()).isEqualTo("13.13");
            assertThat(price.getTotalDue().getAmount()).isEqualTo("275.63");
        }
    }

    @Nested
    class IT_P5_WadiumReducesAmountDue {
        @Test
        void shouldDeductWadiumFromNetto() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Wadium deduction", "1000.00", pricingConfigCar()), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            BigDecimal hammer = new BigDecimal(price.getHammerPrice().getAmount());
            BigDecimal wadium = new BigDecimal(price.getWadiumOffset().getAmount());
            BigDecimal netto = new BigDecimal(price.getNetto().getAmount());
            assertThat(netto).isEqualByComparingTo(hammer.subtract(wadium));
        }
    }

    @Nested
    class IT_P6_NoWadiumNettoEqualsHammer {
        @Test
        void shouldReturnNettoEqualToHammerWhenNoWadium() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("No wadium", "100.00", pricingConfigVatOnly("0.23")), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getWadiumOffset().getAmount()).isEqualTo("0.00");
            assertThat(price.getNetto().getAmount()).isEqualTo(price.getHammerPrice().getAmount());
        }
    }

    @Nested
    class IT_P7_NonExcisableProductNoExciseComponent {
        @Test
        void shouldReturnZeroExciseForNonExcisableProduct() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Non-excisable", "6000.00", pricingConfigVatOnly("0.23")), API_VERSION);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getExcise().getAmount()).isEqualTo("0.00");
            assertThat(price.getCustomsDuty().getAmount()).isEqualTo("0.00");
        }
    }

    @Nested
    class IT_P8_FullFlowCreateToPrice {
        @Test
        void shouldCompleteFullPricingFlow() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Full flow", "50000.00", pricingConfigCar()), API_VERSION);

            DepositWadiumRequest wadiumReq = new DepositWadiumRequest();
            wadiumReq.setAmount(pln("2500.00"));
            WadiumResponse wadium = buyerAuctionsApi.depositWadium(
                auction.getAuctionId(), wadiumReq, API_VERSION);

            assertThat(wadium.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);

            AuctionPriceBreakdownResponse price = auctionsApi.getAuctionPrice(
                auction.getAuctionId(), API_VERSION);

            assertThat(price.getTotalDue().getAmount()).isNotBlank();
            BigDecimal totalDue = new BigDecimal(price.getTotalDue().getAmount());
            BigDecimal netto = new BigDecimal(price.getNetto().getAmount());
            BigDecimal excise = new BigDecimal(price.getExcise().getAmount());
            BigDecimal customs = new BigDecimal(price.getCustomsDuty().getAmount());
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            assertThat(totalDue).isEqualByComparingTo(netto.add(excise).add(customs).add(vat));
        }
    }

    @Nested
    class IT_P9_WadiumRefundOnLoss {
        @Test
        void shouldRefundWadiumWhenBidderLosesAuction() throws Exception {
            AuctionResponse auction = auctionsApi.createAuction(
                createAuctionWith("Refund wadium", "10000.00", pricingConfigCar()), API_VERSION);

            DepositWadiumRequest wadiumReq = new DepositWadiumRequest();
            wadiumReq.setAmount(pln("500.00"));
            WadiumResponse wadium = buyerAuctionsApi.depositWadium(
                auction.getAuctionId(), wadiumReq, API_VERSION);
            assertThat(wadium.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);

            RefundWadiumRequest refundReq = new RefundWadiumRequest();
            refundReq.setPartyId(auction.getAuctionId());

            WadiumRefundResponse refund = buyerAuctionsApi.refundWadium(
                auction.getAuctionId(), refundReq, API_VERSION);

            assertThat(refund.getStatus()).isEqualTo(WadiumRefundResponse.StatusEnum.REFUNDED);
            assertThat(refund.getRefundAmount().getAmount()).isEqualTo("500.00");
        }
    }
}
