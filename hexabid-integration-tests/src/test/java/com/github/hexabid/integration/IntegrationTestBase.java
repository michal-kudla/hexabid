package com.github.hexabid.integration;

import com.github.hexabid.contract.client.ApiClient;
import com.github.hexabid.contract.client.api.AuctionsApi;
import com.github.hexabid.contract.client.api.InventoryApi;
import com.github.hexabid.contract.client.api.ParticipationApi;
import com.github.hexabid.contract.client.api.ProductsApi;
import com.github.hexabid.contract.client.model.*;
import org.junit.jupiter.api.BeforeAll;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Shared base class for all Hexabid black-box integration tests.
 *
 * <p>Provides pre-configured OpenAPI-generated API clients for both seller and buyer personas,
 * along with common factory methods for domain value objects (Money, PricingConfig, Quantity).
 * Each test class inherits the same client setup, eliminating duplication across test files.</p>
 *
 * <h3>Design rationale</h3>
 * <ul>
 *   <li><strong>Generated client over raw HTTP</strong> — the OpenAPI Java native client gives us
 *       compile-time type safety and automatic (de)serialization, making tests readable and
 *       refactor-safe.</li>
 *   <li><strong>Two personas</strong> — seller creates inventory and auctions; buyer deposits
 *       wadium and triggers pricing. This mirrors the real-world actor separation.</li>
 *   <li><strong>Base URL and credentials</strong> — centralized here so a single change
 *       propagates to all tests (e.g., switching to a staging environment).</li>
 * </ul>
 */
public abstract class IntegrationTestBase {

    public static final String BASE_URL = "http://localhost:18080/hexabid";
    public static final String API_VERSION = "1";
    public static final String SELLER_USER = "user";
    public static final String SELLER_PASS = "password";
    public static final String BUYER_USER = "admin";
    public static final String BUYER_PASS = "password";

    public static AuctionsApi sellerAuctionsApi;
    public static AuctionsApi buyerAuctionsApi;
    public static ProductsApi sellerProductsApi;
    public static InventoryApi sellerInventoryApi;
    public static InventoryApi buyerInventoryApi;
    public static ParticipationApi sellerParticipationApi;
    public static ParticipationApi buyerParticipationApi;

    @BeforeAll
    public static void setupApiClients() {
        ApiClient sellerClient = apiClientFor(SELLER_USER, SELLER_PASS);
        ApiClient buyerClient = apiClientFor(BUYER_USER, BUYER_PASS);

        sellerAuctionsApi = new AuctionsApi(sellerClient);
        buyerAuctionsApi = new AuctionsApi(buyerClient);
        sellerProductsApi = new ProductsApi(sellerClient);
        sellerInventoryApi = new InventoryApi(sellerClient);
        buyerInventoryApi = new InventoryApi(buyerClient);
        sellerParticipationApi = new ParticipationApi(sellerClient);
        buyerParticipationApi = new ParticipationApi(buyerClient);
    }

    protected static ApiClient apiClientFor(String user, String pass) {
        ApiClient client = new ApiClient();
        client.updateBaseUri(BASE_URL);
        client.setRequestInterceptor(builder -> {
            builder.header("X-API-Version", API_VERSION);
            builder.header("Authorization", basicAuth(user, pass));
        });
        return client;
    }

    protected static ApiClient anonymousApiClient() {
        ApiClient client = new ApiClient();
        client.updateBaseUri(BASE_URL);
        client.setRequestInterceptor(builder -> {
            builder.header("X-API-Version", API_VERSION);
        });
        return client;
    }

    public static String basicAuth(String user, String pass) {
        return "Basic " + Base64.getEncoder()
                .encodeToString((user + ":" + pass).getBytes(StandardCharsets.UTF_8));
    }

    // ── Money factory ──────────────────────────────────────────────────

    public static Money pln(String amount) {
        Money m = new Money();
        m.setAmount(amount);
        m.setCurrency("PLN");
        return m;
    }

    // ── Quantity factories (generated inner types) ────────────────────

    public static CreateBatchRequestQuantity batchQty(String amount, String unit) {
        CreateBatchRequestQuantity q = new CreateBatchRequestQuantity();
        q.setAmount(amount);
        q.setUnit(unit);
        return q;
    }

    public static CreateInventoryInstanceRequestQuantity instanceQty(String amount, String unit) {
        CreateInventoryInstanceRequestQuantity q = new CreateInventoryInstanceRequestQuantity();
        q.setAmount(amount);
        q.setUnit(unit);
        return q;
    }

    // ── PricingConfig factories ───────────────────────────────────────

    /**
     * Domestic car: 5% wadium, 23% VAT, 3.1% excise (percentage), not imported.
     * Classic Polish auction scenario for a passenger vehicle.
     */
    public static PricingConfig pricingConfigCar() {
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

    /**
     * Imported flour: fixed 50 PLN wadium, 5% VAT, no excise, 5% customs duty.
     * Typical scenario for agricultural commodities imported from the EU.
     */
    public static PricingConfig pricingConfigImportedFlour() {
        PricingConfig c = new PricingConfig();
        c.setWadiumStrategy(WadiumStrategy.FIXED);
        c.setWadiumFixedAmount(pln("50.00"));
        c.setVatRate("0.05");
        c.setIsExcisable(false);
        c.setIsImported(true);
        c.setCustomsDutyRate("0.05");
        return c;
    }

    /**
     * VAT-only config: no wadium, no excise, no customs.
     * Used for simple domestic goods without special tax treatment.
     */
    public static PricingConfig pricingConfigVatOnly(String vatRate) {
        PricingConfig c = new PricingConfig();
        c.setVatRate(vatRate);
        c.setIsExcisable(false);
        c.setIsImported(false);
        return c;
    }

    /**
     * Imported excisable car: 10% wadium, 23% VAT, 18% excise, 10% customs duty.
     * Full tax breakdown scenario — every pricing component is active.
     */
    public static PricingConfig pricingConfigImportedExcisableCar() {
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

    // ── Auction request factory ────────────────────────────────────────

    public static CreateAuctionRequest auctionRequest(String title, String startingPrice) {
        CreateAuctionRequest req = new CreateAuctionRequest();
        req.setTitle(unique(title));
        req.setStartingPrice(pln(startingPrice));
        req.setEndsAt(OffsetDateTime.now().plusHours(4));
        return req;
    }

    public static CreateAuctionRequest auctionRequest(String title, String startingPrice, PricingConfig config) {
        CreateAuctionRequest req = auctionRequest(title, startingPrice);
        req.setPricingConfig(config);
        return req;
    }

    // ── Product request factory ────────────────────────────────────────

    public static CreateProductTypeRequest productRequest(String name, String description,
                                                    ProductTrackingStrategy strategy, String unit) {
        CreateProductTypeRequest req = new CreateProductTypeRequest();
        req.setName(unique(name));
        req.setDescription(description);
        req.setTrackingStrategy(strategy);
        req.setPreferredUnit(unit);
        return req;
    }

    // ── Lot request factory ────────────────────────────────────────────

    public static CreateLotRequest lotRequest(String title, String description,
                                        UUID inventoryEntryId, SellingMode mode) {
        CreateLotRequest req = new CreateLotRequest();
        req.setTitle(unique(title));
        req.setDescription(description);
        req.setInventoryEntryId(inventoryEntryId);
        req.setSellingMode(mode);
        return req;
    }

    public static CreateLotRequest lotRequestWithReserve(String title, String description,
                                                    UUID inventoryEntryId, SellingMode mode,
                                                    String reserveAmount) {
        CreateLotRequest req = lotRequest(title, description, inventoryEntryId, mode);
        req.setReservePrice(pln(reserveAmount));
        return req;
    }

    // ── Paginated search helpers ──────────────────────────────────────

    public static AuctionListItemResponse findAuctionInBrowse(UUID auctionId) throws Exception {
        return findAuctionInBrowse(auctionId, null);
    }

    public static AuctionListItemResponse findAuctionInBrowse(UUID auctionId, String query) throws Exception {
        String cursor = null;
        for (int page = 0; page < 20; page++) {
            AuctionListResponse result = sellerAuctionsApi.browseAuctions(
                    API_VERSION, query, null, null, 50, cursor);
            var found = result.getItems().stream()
                    .filter(a -> a.getAuctionId().equals(auctionId))
                    .findFirst();
            if (found.isPresent()) return found.get();
            cursor = result.getNextCursor();
            if (cursor == null) break;
        }
        throw new AssertionError("Auction " + auctionId + " not found in browse results");
    }

    public static boolean auctionExistsInMyAuctions(UUID auctionId) throws Exception {
        String cursor = null;
        for (int page = 0; page < 20; page++) {
            AuctionListResponse result = sellerAuctionsApi.browseMyAuctions(
                    API_VERSION, null, null, 50, cursor);
            if (result.getItems().stream().anyMatch(a -> a.getAuctionId().equals(auctionId))) {
                return true;
            }
            cursor = result.getNextCursor();
            if (cursor == null) break;
        }
        return false;
    }

    public static boolean auctionExistsInBuyerMyAuctions(UUID auctionId) throws Exception {
        String cursor = null;
        for (int page = 0; page < 20; page++) {
            AuctionListResponse result = buyerAuctionsApi.browseMyAuctions(
                    API_VERSION, null, null, 50, cursor);
            if (result.getItems().stream().anyMatch(a -> a.getAuctionId().equals(auctionId))) {
                return true;
            }
            cursor = result.getNextCursor();
            if (cursor == null) break;
        }
        return false;
    }

    public static ProductTypeResponse findProductInBrowse(UUID productId) throws Exception {
        String cursor = null;
        for (int page = 0; page < 20; page++) {
            ProductTypeListResponse result = sellerProductsApi.browseProductTypes(
                    API_VERSION, null, null, 50, cursor);
            var found = result.getItems().stream()
                    .filter(p -> p.getProductId().equals(productId))
                    .findFirst();
            if (found.isPresent()) return found.get();
            cursor = result.getNextCursor();
            if (cursor == null) break;
        }
        throw new AssertionError("Product " + productId + " not found in browse results");
    }

    // ── Utility ────────────────────────────────────────────────────────

    public static String unique(String base) {
        return base + " " + UUID.randomUUID();
    }

    /**
     * Calls a discovery endpoint (auth providers / payment gateways) that is not covered
     * by the generated Java client. Uses Java's built-in HttpClient as a lightweight
     * alternative to adding another OpenAPI generator execution.
     */
    public static String getDiscoveryEndpoint(String path) throws Exception {
        return getDiscoveryEndpoint(path, null, null);
    }

    public static String getDiscoveryEndpoint(String path, String user, String pass) throws Exception {
        HttpClient http = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("X-API-Version", API_VERSION)
                .GET();
        if (user != null && pass != null) {
            builder.header("Authorization", basicAuth(user, pass));
        }
        HttpRequest request = builder.build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
