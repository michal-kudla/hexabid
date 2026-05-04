package com.github.hexabid.integration.security;

import com.github.hexabid.contract.client.ApiClient;
import com.github.hexabid.contract.client.ApiException;
import com.github.hexabid.contract.client.api.AuctionsApi;
import com.github.hexabid.contract.client.api.InventoryApi;
import com.github.hexabid.contract.client.api.ProductsApi;
import com.github.hexabid.contract.client.model.*;
import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthSecurityIT extends IntegrationTestBase {

    private static final String RAW_BASE_URL = "http://localhost:18080/hexabid";

    private static ApiClient unauthenticatedClient() {
        ApiClient client = new ApiClient();
        client.updateBaseUri(BASE_URL);
        client.setRequestInterceptor(builder -> {
            builder.header("X-API-Version", API_VERSION);
        });
        return client;
    }

    private static ApiClient wrongCredentialsClient() {
        ApiClient client = new ApiClient();
        client.updateBaseUri(BASE_URL);
        client.setRequestInterceptor(builder -> {
            builder.header("X-API-Version", API_VERSION);
            builder.header("Authorization", basicAuth("hacker", "wrongpass"));
        });
        return client;
    }

    @Nested
    @DisplayName("SEC1 - Unauthenticated POST /api/auctions returns 401")
    class IT_SEC1_UnauthenticatedCreateAuction {

        @Test
        @DisplayName("Should reject auction creation without authentication")
        void shouldRejectUnauthenticatedCreateAuction() throws Exception {
            AuctionsApi anonApi = new AuctionsApi(unauthenticatedClient());

            CreateAuctionRequest req = auctionRequest("SEC1 unauth auction", "1000.00");

            assertThatThrownBy(() -> anonApi.createAuction(req, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(401));
        }
    }

    @Nested
    @DisplayName("SEC2 - Wrong credentials return 401")
    class IT_SEC2_WrongCredentials {

        @Test
        @DisplayName("Should reject request with wrong username/password")
        void shouldRejectWrongCredentials() throws Exception {
            AuctionsApi hackerApi = new AuctionsApi(wrongCredentialsClient());

            CreateAuctionRequest req = auctionRequest("SEC2 wrong creds auction", "1000.00");

            assertThatThrownBy(() -> hackerApi.createAuction(req, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(401));
        }
    }

    @Nested
    @DisplayName("SEC3 - Public endpoints accessible without auth")
    class IT_SEC3_PublicEndpoints {

        @Test
        @DisplayName("Should allow anonymous browsing of auctions")
        void shouldAllowAnonymousBrowseAuctions() throws Exception {
            AuctionsApi anonApi = new AuctionsApi(unauthenticatedClient());

            AuctionListResponse response = anonApi.browseAuctions(
                    API_VERSION, null, null, null, null, null);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Should allow anonymous get auction by ID")
        void shouldAllowAnonymousGetAuction() throws Exception {
            CreateAuctionRequest req = auctionRequest("SEC3 public get auction", "1000.00");
            AuctionResponse created = sellerAuctionsApi.createAuction(req, API_VERSION);

            AuctionsApi anonApi = new AuctionsApi(unauthenticatedClient());
            AuctionResponse fetched = anonApi.getAuctionById(created.getAuctionId(), API_VERSION);
            assertThat(fetched.getAuctionId()).isEqualTo(created.getAuctionId());
        }
    }

    @Nested
    @DisplayName("SEC4 - Auth providers endpoint is public")
    class IT_SEC4_AuthProvidersPublic {

        @Test
        @DisplayName("Should return auth providers without authentication")
        void shouldReturnAuthProvidersWithoutAuth() throws Exception {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RAW_BASE_URL + "/api/auth/providers"))
                    .header("X-API-Version", API_VERSION)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("local");
        }
    }

    @Nested
    @DisplayName("SEC5 - Unauthenticated POST /api/batches returns 401")
    class IT_SEC5_UnauthenticatedCreateBatch {

        @Test
        @DisplayName("Should reject batch creation without authentication")
        void shouldRejectUnauthenticatedCreateBatch() throws Exception {
            InventoryApi anonApi = new InventoryApi(unauthenticatedClient());

            CreateBatchRequest req = new CreateBatchRequest();
            req.setProductId(UUID.randomUUID());
            req.setName("SEC5 unauthorized batch");
            req.setQuantity(batchQty("100", "kg"));

            assertThatThrownBy(() -> anonApi.createBatch(req, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(401));
        }
    }

    @Nested
    @DisplayName("SEC6 - Unauthenticated POST /api/inventory/instances returns 401")
    class IT_SEC6_UnauthenticatedCreateInstance {

        @Test
        @DisplayName("Should reject instance creation without authentication")
        void shouldRejectUnauthenticatedCreateInstance() throws Exception {
            InventoryApi anonApi = new InventoryApi(unauthenticatedClient());

            CreateInventoryInstanceRequest req = new CreateInventoryInstanceRequest();
            req.setProductId(UUID.randomUUID());
            req.setQuantity(instanceQty("100", "kg"));

            assertThatThrownBy(() -> anonApi.createInventoryInstance(req, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(401));
        }
    }

    @Nested
    @DisplayName("SEC7 - Unauthenticated POST /api/products returns 401")
    class IT_SEC7_UnauthenticatedCreateProduct {

        @Test
        @DisplayName("Should reject product creation without authentication")
        void shouldRejectUnauthenticatedCreateProduct() throws Exception {
            ProductsApi anonApi = new ProductsApi(unauthenticatedClient());

            CreateProductTypeRequest req = productRequest("SEC7 unauth product",
                    "Should not be created", ProductTrackingStrategy.UNIQUE, "pcs");

            assertThatThrownBy(() -> anonApi.createProductType(req, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(401));
        }
    }

    @Nested
    @DisplayName("SEC8 - My auctions endpoint requires authentication")
    class IT_SEC8_MyAuctionsRequiresAuth {

        @Test
        @DisplayName("Should reject /api/me/auctions without authentication")
        void shouldRejectMyAuctionsWithoutAuth() throws Exception {
            AuctionsApi anonApi = new AuctionsApi(unauthenticatedClient());

            assertThatThrownBy(() -> anonApi.browseMyAuctions(API_VERSION, null, null, null, null))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(401));
        }
    }

    @Nested
    @DisplayName("SEC9 - My bids endpoint requires authentication")
    class IT_SEC9_MyBidsRequiresAuth {

        @Test
        @DisplayName("Should reject /api/me/bids without authentication")
        void shouldRejectMyBidsWithoutAuth() throws Exception {
            AuctionsApi anonApi = new AuctionsApi(unauthenticatedClient());

            assertThatThrownBy(() -> anonApi.browseMyBids(API_VERSION, null, null, null, null))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(401));
        }
    }

    @Nested
    @DisplayName("SEC10 - Wadium deposit requires authentication")
    class IT_SEC10_WadiumRequiresAuth {

        @Test
        @DisplayName("Should reject wadium deposit without authentication")
        void shouldRejectWadiumDepositWithoutAuth() throws Exception {
            AuctionsApi anonApi = new AuctionsApi(unauthenticatedClient());

            CreateAuctionRequest req = auctionRequest("SEC10 wadium auth auction", "10000.00",
                    pricingConfigCar());
            AuctionResponse auction = sellerAuctionsApi.createAuction(req, API_VERSION);

            DepositWadiumRequest depositReq = new DepositWadiumRequest();
            depositReq.setAmount(pln("500.00"));

            assertThatThrownBy(() -> anonApi.depositWadium(auction.getAuctionId(), depositReq, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(401));
        }
    }
}
