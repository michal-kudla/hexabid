package com.github.hexabid.integration.flow;

import com.github.hexabid.contract.client.model.*;
import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuctionLifecycleTransitionsIT extends IntegrationTestBase {

    @Nested
    @DisplayName("LT1 - Auction created in DRAFT status")
    class IT_LT1_CreatedAsDraft {

        @Test
        @DisplayName("Should create auction and verify it starts as DRAFT")
        void shouldCreateAuctionInDraftStatus() throws Exception {
            CreateAuctionRequest req = auctionRequest("LT1 draft auction", "1000.00");
            AuctionResponse auction = sellerAuctionsApi.createAuction(req, API_VERSION);

            assertThat(auction.getStatus()).isEqualTo(AuctionStatus.DRAFT);
            assertThat(auction.getAuctionId()).isNotNull();
            assertThat(auction.getLeadingBidderId()).isNull();
            assertThat(auction.getBids()).isEmpty();
        }
    }

    @Nested
    @DisplayName("LT2 - DRAFT auction visible in my auctions and public browse")
    class IT_LT2_DraftVisibility {

        @Test
        @DisplayName("Should see DRAFT auction in seller's my-auctions and public browse")
        void shouldSeeDraftInBothEndpoints() throws Exception {
            CreateAuctionRequest req = auctionRequest("LT2 visibility auction", "2000.00");
            AuctionResponse auction = sellerAuctionsApi.createAuction(req, API_VERSION);
            UUID auctionId = auction.getAuctionId();

            AuctionListResponse myAuctions = sellerAuctionsApi.browseMyAuctions(
                    API_VERSION, null, null, 50, null);
            boolean foundInMy = myAuctions.getItems().stream()
                    .anyMatch(a -> a.getAuctionId().equals(auctionId));
            if (!foundInMy && myAuctions.getNextCursor() != null) {
                foundInMy = auctionExistsInMyAuctions(auctionId);
            }
            assertThat(foundInMy).isTrue();

            AuctionListResponse publicAuctions = sellerAuctionsApi.browseAuctions(
                    API_VERSION, null, null, null, 50, null);
            boolean foundInPublic = publicAuctions.getItems().stream()
                    .anyMatch(a -> a.getAuctionId().equals(auctionId));
            if (!foundInPublic && publicAuctions.getNextCursor() != null) {
                try {
                    findAuctionInBrowse(auctionId);
                    foundInPublic = true;
                } catch (AssertionError ignored) {
                }
            }
            assertThat(foundInPublic).isTrue();
        }
    }

    @Nested
    @DisplayName("LT3 - Auction with pricing config carries config through lifecycle")
    class IT_LT3_PricingConfigPersisted {

        @Test
        @DisplayName("Should preserve pricing config after creation")
        void shouldPreservePricingConfig() throws Exception {
            CreateAuctionRequest req = auctionRequest("LT3 pricing auction", "50000.00",
                    pricingConfigImportedExcisableCar());
            AuctionResponse auction = sellerAuctionsApi.createAuction(req, API_VERSION);

            assertThat(auction.getPricingConfig()).isNotNull();
            assertThat(auction.getPricingConfig().getVatRate()).isEqualTo("0.23");
            assertThat(auction.getPricingConfig().getIsImported()).isTrue();

            AuctionResponse fetched = sellerAuctionsApi.getAuctionById(auction.getAuctionId(), API_VERSION);
            assertThat(fetched.getAuctionId()).isEqualTo(auction.getAuctionId());
            assertThat(fetched.getTitle()).isEqualTo(auction.getTitle());
        }
    }

    @Nested
    @DisplayName("LT4 - Wadium deposit transitions and verification")
    class IT_LT4_WadiumTransitions {

        @Test
        @DisplayName("Should deposit wadium, verify PAID, then refund and verify REFUNDED")
        void shouldTransitionWadiumFromPaidToRefunded() throws Exception {
            CreateAuctionRequest req = auctionRequest("LT4 wadium transition auction", "10000.00",
                    pricingConfigCar());
            AuctionResponse auction = sellerAuctionsApi.createAuction(req, API_VERSION);

            DepositWadiumRequest depositReq = new DepositWadiumRequest();
            depositReq.setAmount(pln("500.00"));
            WadiumResponse deposit = buyerAuctionsApi.depositWadium(
                    auction.getAuctionId(), depositReq, API_VERSION);

            assertThat(deposit.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);
            assertThat(deposit.getRefundableOnLoss()).isTrue();
            assertThat(deposit.getDeductibleOnWin()).isTrue();

            RefundWadiumRequest refundReq = new RefundWadiumRequest();
            refundReq.setPartyId(auction.getAuctionId());
            WadiumRefundResponse refund = buyerAuctionsApi.refundWadium(
                    auction.getAuctionId(), refundReq, API_VERSION);

            assertThat(refund.getStatus()).isEqualTo(WadiumRefundResponse.StatusEnum.REFUNDED);
            assertThat(refund.getRefundAmount().getAmount()).isEqualTo("500.00");
        }
    }

    @Nested
    @DisplayName("LT5 - Multiple wadium deposits on same auction")
    class IT_LT5_MultipleWadiumDeposits {

        @Test
        @DisplayName("Should allow multiple wadium deposits and verify each is independent")
        void shouldAllowMultipleWadiumDeposits() throws Exception {
            CreateAuctionRequest req = auctionRequest("LT5 multi-wadium auction", "20000.00",
                    pricingConfigCar());
            AuctionResponse auction = sellerAuctionsApi.createAuction(req, API_VERSION);

            DepositWadiumRequest deposit1 = new DepositWadiumRequest();
            deposit1.setAmount(pln("500.00"));
            WadiumResponse w1 = buyerAuctionsApi.depositWadium(auction.getAuctionId(), deposit1, API_VERSION);
            assertThat(w1.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);

            DepositWadiumRequest deposit2 = new DepositWadiumRequest();
            deposit2.setAmount(pln("1000.00"));
            WadiumResponse w2 = buyerAuctionsApi.depositWadium(auction.getAuctionId(), deposit2, API_VERSION);
            assertThat(w2.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);
        }
    }

    @Nested
    @DisplayName("LT6 - Wadium refund then redeposit")
    class IT_LT6_WadiumRefundRedeposit {

        @Test
        @DisplayName("Should allow redeposit after refund")
        void shouldAllowRedepositAfterRefund() throws Exception {
            CreateAuctionRequest req = auctionRequest("LT6 refund-redeposit auction", "15000.00",
                    pricingConfigCar());
            AuctionResponse auction = sellerAuctionsApi.createAuction(req, API_VERSION);

            DepositWadiumRequest depositReq = new DepositWadiumRequest();
            depositReq.setAmount(pln("750.00"));
            buyerAuctionsApi.depositWadium(auction.getAuctionId(), depositReq, API_VERSION);

            RefundWadiumRequest refundReq = new RefundWadiumRequest();
            refundReq.setPartyId(auction.getAuctionId());
            WadiumRefundResponse refund = buyerAuctionsApi.refundWadium(
                    auction.getAuctionId(), refundReq, API_VERSION);
            assertThat(refund.getStatus()).isEqualTo(WadiumRefundResponse.StatusEnum.REFUNDED);

            DepositWadiumRequest deposit2Req = new DepositWadiumRequest();
            deposit2Req.setAmount(pln("800.00"));
            WadiumResponse redeposit = buyerAuctionsApi.depositWadium(
                    auction.getAuctionId(), deposit2Req, API_VERSION);
            assertThat(redeposit.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);
            assertThat(redeposit.getAmount().getAmount()).isEqualTo("800.00");
        }
    }

    @Nested
    @DisplayName("LT7 - Price breakdown reflects pricing config wadium")
    class IT_LT7_PriceBreakdownWithWadium {

        @Test
        @DisplayName("Should show wadium offset based on pricing config in price breakdown")
        void shouldReflectWadiumInPriceBreakdown() throws Exception {
            CreateAuctionRequest req = auctionRequest("LT7 price+wadium auction", "10000.00",
                    pricingConfigCar());
            AuctionResponse auction = sellerAuctionsApi.createAuction(req, API_VERSION);

            AuctionPriceBreakdownResponse priceBefore = sellerAuctionsApi.getAuctionPrice(
                    auction.getAuctionId(), API_VERSION);

            assertThat(priceBefore.getWadiumOffset()).isNotNull();
            assertThat(new BigDecimal(priceBefore.getWadiumOffset().getAmount()))
                    .isEqualByComparingTo(new BigDecimal("500.00"));

            DepositWadiumRequest depositReq = new DepositWadiumRequest();
            depositReq.setAmount(pln("500.00"));
            buyerAuctionsApi.depositWadium(auction.getAuctionId(), depositReq, API_VERSION);

            AuctionPriceBreakdownResponse priceAfter = sellerAuctionsApi.getAuctionPrice(
                    auction.getAuctionId(), API_VERSION);

            assertThat(priceAfter.getWadiumOffset()).isNotNull();
            assertThat(new BigDecimal(priceAfter.getWadiumOffset().getAmount()))
                    .isEqualByComparingTo(new BigDecimal("500.00"));
        }
    }

    @Nested
    @DisplayName("LT8 - Current user profile reflects authenticated identity")
    class IT_LT8_UserProfileIdentity {

        @Test
        @DisplayName("Should return correct profile for seller and buyer")
        void shouldReturnCorrectProfile() throws Exception {
            CurrentUserProfileResponse sellerProfile = sellerAuctionsApi.getCurrentUserProfile(API_VERSION);
            assertThat(sellerProfile.getPartyId()).isEqualTo("local:user");
            assertThat(sellerProfile.getProvider()).isEqualTo("local");

            CurrentUserProfileResponse buyerProfile = buyerAuctionsApi.getCurrentUserProfile(API_VERSION);
            assertThat(buyerProfile.getPartyId()).isEqualTo("local:admin");
            assertThat(buyerProfile.getProvider()).isEqualTo("local");
        }
    }
}
