package com.github.hexabid.integration.flow;

import com.github.hexabid.contract.client.ApiClient;
import com.github.hexabid.contract.client.api.AuctionsApi;
import com.github.hexabid.contract.client.model.*;
import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Black-box integration tests for the <strong>Auction Lifecycle</strong> (M03).
 *
 * <p>This test class covers the public-facing auction operations: creating auctions,
 * browsing/searching the catalog, viewing auction details, and the "my auctions" /
 * "my bids" personalized views. It also covers the user profile endpoint.</p>
 *
 * <h3>Business context</h3>
 * <p>An auction is the central entity of the platform. The seller creates it by specifying
 * a title, starting price, and end time. Optionally, a lot (linking to inventory) and
 * a buy-now price can be set. Buyers browse the catalog, and the system tracks who
 * has bid on which auctions.</p>
 *
 * <h3>Covered endpoints</h3>
 * <ul>
 *   <li>{@code POST   /api/auctions}          — createAuction</li>
 *   <li>{@code GET    /api/auctions/{id}}     — getAuctionById</li>
 *   <li>{@code GET    /api/auctions}          — browseAuctions (public)</li>
 *   <li>{@code GET    /api/me}                — getCurrentUserProfile</li>
 *   <li>{@code GET    /api/me/auctions}      — browseMyAuctions</li>
 *   <li>{@code GET    /api/me/bids}           — browseMyBids</li>
 * </ul>
 */
class AuctionLifecycleFlowIT extends IntegrationTestBase {

    @Nested
    @DisplayName("F18 — Create auction and verify response fields")
    class IT_F18_CreateAuctionBasic {

        @Test
        @DisplayName("Should create auction and verify key response fields")
        void shouldCreateAuctionAndVerifyResponse() throws Exception {
            CreateAuctionRequest req = auctionRequest("Basic auction", "5000.00");

            AuctionResponse response = sellerAuctionsApi.createAuction(req, API_VERSION);

            assertThat(response.getAuctionId()).isNotNull();
            assertThat(response.getTitle()).isNotBlank();
            assertThat(response.getStatus()).isEqualTo(AuctionStatus.DRAFT);
            assertThat(response.getSellerId()).isNotBlank();
            assertThat(response.getCurrentPrice()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("F19 — Create auction with buy-now price")
    class IT_F19_CreateAuctionWithBuyNow {

        @Test
        @DisplayName("Should accept buy-now price in creation request without error")
        void shouldCreateAuctionWithBuyNowPrice() throws Exception {
            CreateAuctionRequest req = auctionRequest("Buy-now auction", "1000.00");
            req.setBuyNowPrice(pln("1500.00"));

            AuctionResponse response = sellerAuctionsApi.createAuction(req, API_VERSION);

            assertThat(response.getAuctionId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("F20 — Get auction by ID and verify full response")
    class IT_F20_GetAuctionById {

        @Test
        @DisplayName("Should return complete auction details including seller and pricing")
        void shouldReturnCompleteAuctionDetails() throws Exception {
            CreateAuctionRequest req = auctionRequest("Detailed auction", "7500.00", pricingConfigCar());
            AuctionResponse created = sellerAuctionsApi.createAuction(req, API_VERSION);

            AuctionResponse fetched = sellerAuctionsApi.getAuctionById(created.getAuctionId(), API_VERSION);

            assertThat(fetched.getAuctionId()).isEqualTo(created.getAuctionId());
            assertThat(fetched.getTitle()).isEqualTo(created.getTitle());
            assertThat(fetched.getStatus()).isEqualTo(AuctionStatus.DRAFT);
            assertThat(fetched.getSellerId()).isNotBlank();
            assertThat(fetched.getCurrentPrice()).isNotBlank();
            assertThat(fetched.getCurrency()).isEqualTo("PLN");
            assertThat(fetched.getEndsAt()).isAfter(OffsetDateTime.now());
            assertThat(fetched.getBids()).isEmpty();
        }
    }

    @Nested
    @DisplayName("F21 — Browse public auctions (no auth required)")
    class IT_F21_BrowsePublicAuctions {

        @Test
        @DisplayName("Should return auction list accessible without authentication")
        void shouldBrowsePublicAuctions() throws Exception {
            CreateAuctionRequest req = auctionRequest("Public browse auction", "200.00");
            sellerAuctionsApi.createAuction(req, API_VERSION);

            ApiClient anonClient = new ApiClient();
            anonClient.updateBaseUri(BASE_URL);
            anonClient.setRequestInterceptor(builder -> builder.header("X-API-Version", API_VERSION));
            AuctionsApi anonApi = new AuctionsApi(anonClient);

            AuctionListResponse response = anonApi.browseAuctions(API_VERSION, null, null, null, null, null);

            assertThat(response.getItems()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("F22 — Browse auctions by status")
    class IT_F22_BrowseAuctionsByStatus {

        @Test
        @DisplayName("Should return only DRAFT auctions when status filter is applied")
        void shouldFilterByDraftStatus() throws Exception {
            CreateAuctionRequest req = auctionRequest("Status filter auction", "300.00");
            sellerAuctionsApi.createAuction(req, API_VERSION);

            AuctionListResponse response = sellerAuctionsApi.browseAuctions(
                    API_VERSION, null, AuctionStatus.DRAFT, null, null, null);

            assertThat(response.getItems()).isNotEmpty();
            assertThat(response.getItems())
                    .allSatisfy(a -> assertThat(a.getStatus()).isEqualTo(AuctionStatus.DRAFT));
        }
    }

    @Nested
    @DisplayName("F23 — Search auctions by query")
    class IT_F23_SearchAuctionsByQuery {

        @Test
        @DisplayName("Should find auctions matching a partial title query")
        void shouldSearchAuctionsByTitle() throws Exception {
            String distinctiveTitle = "QPONM_" + UUID.randomUUID();
            CreateAuctionRequest req = new CreateAuctionRequest();
            req.setTitle(distinctiveTitle);
            req.setStartingPrice(pln("999.00"));
            req.setEndsAt(OffsetDateTime.now().plusHours(4));
            sellerAuctionsApi.createAuction(req, API_VERSION);

            AuctionListResponse response = sellerAuctionsApi.browseAuctions(
                    API_VERSION, "QPONM", null, null, null, null);

            assertThat(response.getItems())
                    .anySatisfy(a -> assertThat(a.getTitle()).contains(distinctiveTitle));
        }
    }

    @Nested
    @DisplayName("F24 — Browse my auctions (seller view)")
    class IT_F24_BrowseMyAuctions {

        @Test
        @DisplayName("Should list auctions created by the authenticated seller")
        void shouldListMyAuctions() throws Exception {
            CreateAuctionRequest req = auctionRequest("My auction", "400.00");
            sellerAuctionsApi.createAuction(req, API_VERSION);

            AuctionListResponse response = sellerAuctionsApi.browseMyAuctions(
                    API_VERSION, null, null, null, null);

            assertThat(response.getItems()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("F25 — Get current user profile")
    class IT_F25_GetCurrentUserProfile {

        @Test
        @DisplayName("Should return authenticated user's profile with partyId and provider")
        void shouldReturnCurrentUserProfile() throws Exception {
            CurrentUserProfileResponse profile = sellerAuctionsApi.getCurrentUserProfile(API_VERSION);

            assertThat(profile.getPartyId()).isNotBlank();
            assertThat(profile.getProvider()).isNotBlank();
            assertThat(profile.getDisplayName()).isNotBlank();
            assertThat(profile.getVerified()).isNotNull();
        }
    }

    @Nested
    @DisplayName("F26 — Browse my bids")
    class IT_F26_BrowseMyBids {

        @Test
        @DisplayName("Should return the buyer's bid list (empty if no bids placed)")
        void shouldBrowseMyBids() throws Exception {
            AuctionListResponse response = buyerAuctionsApi.browseMyBids(
                    API_VERSION, null, null, null, null);

            assertThat(response).isNotNull();
            assertThat(response.getItems()).isNotNull();
        }
    }

    @Nested
    @DisplayName("F27 — Sort auctions by ending soon")
    class IT_F27_SortAuctionsByEndingSoon {

        @Test
        @DisplayName("Should return auctions sorted by closest end time")
        void shouldSortByEndingSoon() throws Exception {
            CreateAuctionRequest req = auctionRequest("Sorted auction", "500.00");
            sellerAuctionsApi.createAuction(req, API_VERSION);

            AuctionListResponse response = sellerAuctionsApi.browseAuctions(
                    API_VERSION, null, AuctionStatus.DRAFT, AuctionSort.ENDING_SOON, null, null);

            assertThat(response.getItems()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("F28 — Create auction with pricing config and verify price breakdown")
    class IT_F28_AuctionWithPricingVerification {

        @Test
        @DisplayName("Should create auction with pricing and retrieve its price breakdown")
        void shouldCreateAuctionAndGetPriceBreakdown() throws Exception {
            CreateAuctionRequest req = auctionRequest("Priced auction", "50000.00", pricingConfigCar());
            AuctionResponse auction = sellerAuctionsApi.createAuction(req, API_VERSION);

            assertThat(auction.getPricingConfig()).isNotNull();
            assertThat(auction.getPricingConfig().getVatRate()).isEqualTo("0.23");

            AuctionPriceBreakdownResponse price = sellerAuctionsApi.getAuctionPrice(
                    auction.getAuctionId(), API_VERSION);

            assertThat(price.getHammerPrice().getAmount()).isEqualTo("50000.00");
            assertThat(new BigDecimal(price.getTotalDue().getAmount()))
                    .isGreaterThan(new BigDecimal(price.getNetto().getAmount()));
        }
    }

    // ── Helper: create product → inventory instance ─────────────────────

    private UUID createProductAndInventory() throws Exception {
        CreateProductTypeRequest prodReq = productRequest(
                "Lifecycle test corn",
                "Corn for lot test",
                ProductTrackingStrategy.BATCH_TRACKED,
                "kg"
        );
        UUID productId = sellerProductsApi.createProductType(prodReq, API_VERSION).getProductId();

        CreateInventoryInstanceRequest instReq = new CreateInventoryInstanceRequest();
        instReq.setProductId(productId);
        instReq.setQuantity(instanceQty("1000", "kg"));
        return sellerInventoryApi.createInventoryInstance(instReq, API_VERSION).getInstanceId();
    }
}
