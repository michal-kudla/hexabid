package com.github.hexabid.integration.flow;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BasicCommerceFlowApiIT {

    private static final String API_VERSION = "1";
    private static final String BASE_URL = "http://localhost:8090";
    private static final String SELLER_USER = "user";
    private static final String SELLER_PASS = "password";
    private static final String BUYER_USER = "admin";
    private static final String BUYER_PASS = "password";
    private static HttpClient httpClient;

    @BeforeAll
    static void setupHttpClient() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  BASIC COMMERCE FLOW API TEST - SETUP                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
        System.out.println("Base URL:     " + BASE_URL);
        System.out.println("API Version:   " + API_VERSION);
        System.out.println("Seller:        " + SELLER_USER + " / " + SELLER_PASS);
        System.out.println("Buyer:         " + BUYER_USER + " / " + BUYER_PASS);
        System.out.println("─────────────────────────────────────────────────────────────────────");
        httpClient = HttpClient.newHttpClient();
        System.out.println("✓ HTTP Client initialized\n");
    }

    private static String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private void logRequest(String method, String url, String body, String authUser) {
        System.out.println("┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ " + method + " " + url);
        System.out.println("├─────────────────────────────────────────────────────────────────────┤");
        System.out.println("│ Headers:");
        System.out.println("│   Content-Type: application/json");
        System.out.println("│   X-API-Version: " + API_VERSION);
        if (authUser != null) {
            System.out.println("│   Authorization: Basic [user=" + authUser + "]");
        }
        System.out.println("├─────────────────────────────────────────────────────────────────────┤");
        if (body != null) {
            System.out.println("│ Payload:");
            System.out.println(body.lines().map(line -> "│   " + line).reduce((a, b) -> a + "\n" + b).orElse("│   " + body));
        } else {
            System.out.println("│ (no payload)");
        }
        System.out.println("└─────────────────────────────────────────────────────────────────────┘");
    }

    private void logResponse(HttpResponse<String> response, String operation) {
        System.out.println("┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ RESPONSE for: " + operation);
        System.out.println("├─────────────────────────────────────────────────────────────────────┤");
        int status = response.statusCode();
        String statusIcon = status == 200 ? "✓" : status == 201 ? "✓" : status == 401 ? "🔒" : "✗";
        System.out.println("│ Status: " + status + " " + statusIcon);
        System.out.println("├─────────────────────────────────────────────────────────────────────┤");
        System.out.println("│ Body:");
        String body = response.body();
        if (body != null && !body.isEmpty()) {
            System.out.println(body.lines().map(line -> "│   " + line).reduce((a, b) -> a + "\n" + b).orElse("│   " + body));
        } else {
            System.out.println("│   (empty)");
        }
        System.out.println("└─────────────────────────────────────────────────────────────────────┘\n");
    }

    @Test
    void shouldCoverBaselineBackendFlowUsingRealHttpEndpoints() throws Exception {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  BASIC COMMERCE FLOW TEST                                        ║");
        System.out.println("║  Verifying complete auction lifecycle via HTTP endpoints           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");

        String auctionTitle = "IT flow " + UUID.randomUUID();
        OffsetDateTime endsAt = OffsetDateTime.now().plusHours(4).truncatedTo(ChronoUnit.SECONDS);

        String requestBody = """
                {
                    "title": "%s",
                    "startingPrice": {
                        "amount": "100.00",
                        "currency": "PLN"
                    },
                    "endsAt": "%s"
                }
                """.formatted(auctionTitle, endsAt.toString());

        System.out.println("STEP 1: Create new auction as SELLER");
        System.out.println("─────────────────────────────────────");
        System.out.println("Title:  " + auctionTitle);
        System.out.println("Price:  100.00 PLN");
        System.out.println("Ends:   " + endsAt + "\n");

        String endpoint = BASE_URL + "/api/auctions";
        logRequest("POST", endpoint, requestBody, SELLER_USER);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("X-API-Version", API_VERSION)
                .header("Authorization", basicAuthHeader(SELLER_USER, SELLER_PASS))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());

        logResponse(createResponse, "Create Auction");

        assertThat(createResponse.statusCode()).isEqualTo(201);
        String responseBody = createResponse.body();
        assertThat(responseBody).contains("auctionId");

        String auctionId = extractJsonValue(responseBody, "auctionId");
        System.out.println("✓ Auction created: " + auctionId + "\n");

        System.out.println("STEP 2: Retrieve auction details (public endpoint)");
        System.out.println("─────────────────────────────────────────────────\n");

        endpoint = BASE_URL + "/api/auctions/" + auctionId;
        logRequest("GET", endpoint, null, null);

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("X-API-Version", API_VERSION)
                .GET()
                .build();

        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

        logResponse(getResponse, "Get Auction by ID");

        assertThat(getResponse.statusCode()).isEqualTo(200);
        assertThat(getResponse.body()).contains(auctionTitle);
        assertThat(getResponse.body()).contains("OPEN");
        System.out.println("✓ Auction verified: title='" + auctionTitle + "', status=OPEN\n");

        System.out.println("STEP 3: List SELLER's auctions (/api/me/auctions)");
        System.out.println("────────────────────────────────────────────────\n");

        endpoint = BASE_URL + "/api/me/auctions";
        logRequest("GET", endpoint, null, SELLER_USER);

        HttpRequest meRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("X-API-Version", API_VERSION)
                .header("Authorization", basicAuthHeader(SELLER_USER, SELLER_PASS))
                .GET()
                .build();

        HttpResponse<String> meResponse = httpClient.send(meRequest, HttpResponse.BodyHandlers.ofString());

        logResponse(meResponse, "Get My Auctions");

        assertThat(meResponse.statusCode()).isEqualTo(200);
        System.out.println("✓ Seller's auctions retrieved successfully\n");

        System.out.println("STEP 4: List payment gateways (/api/payments/gateways) as BUYER");
        System.out.println("────────────────────────────────────────────────────────────────\n");

        endpoint = BASE_URL + "/api/payments/gateways";
        logRequest("GET", endpoint, null, BUYER_USER);

        HttpRequest paymentsRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("X-API-Version", API_VERSION)
                .header("Authorization", basicAuthHeader(BUYER_USER, BUYER_PASS))
                .GET()
                .build();

        HttpResponse<String> paymentsResponse = httpClient.send(paymentsRequest, HttpResponse.BodyHandlers.ofString());

        logResponse(paymentsResponse, "Get Payment Gateways");

        assertThat(paymentsResponse.statusCode()).isEqualTo(200);
        System.out.println("✓ Payment gateways retrieved successfully\n");

        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  BASIC FLOW TEST: ALL STEPS PASSED ✓                             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");
        System.out.println("Flow summary:");
        System.out.println("  1. ✓ Create auction (POST /api/auctions)");
        System.out.println("  2. ✓ Get auction details (GET /api/auctions/{id})");
        System.out.println("  3. ✓ List seller's auctions (GET /api/me/auctions)");
        System.out.println("  4. ✓ List payment gateways (GET /api/payments/gateways)\n");
    }

    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            searchKey = "\"" + key + "\": ";
            startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return null;
            startIndex += searchKey.length();
        } else {
            startIndex += searchKey.length();
        }
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) return null;
        return json.substring(startIndex, endIndex);
    }
}
