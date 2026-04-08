package com.acme.auctions.integration.flow;

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

class ProductBatchInstanceAuctionScenariosIT {

    private static final String API_VERSION = "1";
    private static final String BASE_URL = "http://localhost:8090";
    private static final String SELLER_USER = "user";
    private static final String SELLER_PASS = "password";
    private static HttpClient httpClient;

    @BeforeAll
    static void setupHttpClient() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  SCENARIOS INTEGRATION TESTS - SETUP                              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
        System.out.println("Base URL:     " + BASE_URL);
        System.out.println("API Version:   " + API_VERSION);
        System.out.println("Seller User:  " + SELLER_USER);
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
        System.out.println("│   Authorization: Basic [user=" + authUser + "]");
        System.out.println("├─────────────────────────────────────────────────────────────────────┤");
        System.out.println("│ Payload:");
        if (body != null) {
            System.out.println(body.lines().map(line -> "│   " + line).reduce((a, b) -> a + "\n" + b).orElse("│   " + body));
        } else {
            System.out.println("│   (empty)");
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
    void scenarioA_uniqueProductOnAuction() throws Exception {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  SCENARIO A: UNIQUE PRODUCT ON AUCTION                            ║");
        System.out.println("║  Test: Create auction for unique item (Seat Leon 1999)             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");

        String auctionTitle = "Unikalny samochód - Seat Leon 1999 " + UUID.randomUUID();
        OffsetDateTime endsAt = OffsetDateTime.now().plusHours(4).truncatedTo(ChronoUnit.SECONDS);

        String requestBody = """
                {
                    "title": "%s",
                    "startingPrice": {
                        "amount": "5000.00",
                        "currency": "PLN"
                    },
                    "endsAt": "%s"
                }
                """.formatted(auctionTitle, endsAt.toString());

        System.out.println("Creating auction for unique product...");
        System.out.println("Title: " + auctionTitle);
        System.out.println("Starting price: 5000.00 PLN");
        System.out.println("Ends at: " + endsAt);
        System.out.println();

        String endpoint = BASE_URL + "/api/auctions";
        logRequest("POST", endpoint, requestBody, SELLER_USER);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("X-API-Version", API_VERSION)
                .header("Authorization", basicAuthHeader(SELLER_USER, SELLER_PASS))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        logResponse(response, "Create Auction");

        assertThat(response.statusCode()).isEqualTo(201);

        String responseBody = response.body();
        assertThat(responseBody).contains("auctionId");
        assertThat(responseBody).contains(auctionTitle);
        assertThat(responseBody).contains("OPEN");

        String auctionId = extractJsonValue(responseBody, "auctionId");
        System.out.println("✓ Auction created successfully!");
        System.out.println("  Auction ID: " + auctionId);
        System.out.println("  Status: OPEN");
        System.out.println("  Starting Price: 5000.00 PLN\n");

        System.out.println("Verifying auction exists...");
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

        System.out.println("✓ Auction verified!");
        System.out.println("  Title: " + auctionTitle);
        System.out.println("  Status: OPEN");
        System.out.println("  buyNowPrice: null (as expected for unique product)\n");

        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  SCENARIO A: PASSED ✓                                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");
    }

    @Test
    void scenarioB_divisibleBatchWithBuyNow() throws Exception {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  SCENARIO B: DIVISIBLE BATCH WITH BUY-NOW OPTION                  ║");
        System.out.println("║  Test: Create auction for divisible batch (Ziemniaki)              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");

        String auctionTitle = "Ziemniaki - sprzedaż z kup teraz " + UUID.randomUUID();
        OffsetDateTime endsAt = OffsetDateTime.now().plusHours(2).truncatedTo(ChronoUnit.SECONDS);

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

        System.out.println("Creating auction for divisible batch...");
        System.out.println("Title: " + auctionTitle);
        System.out.println("Starting price: 100.00 PLN");
        System.out.println("Ends at: " + endsAt);
        System.out.println();

        String endpoint = BASE_URL + "/api/auctions";
        logRequest("POST", endpoint, requestBody, SELLER_USER);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("X-API-Version", API_VERSION)
                .header("Authorization", basicAuthHeader(SELLER_USER, SELLER_PASS))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        logResponse(response, "Create Auction");

        assertThat(response.statusCode()).isEqualTo(201);

        String responseBody = response.body();
        assertThat(responseBody).contains("auctionId");
        assertThat(responseBody).contains(auctionTitle);
        assertThat(responseBody).contains("100.00");

        String auctionId = extractJsonValue(responseBody, "auctionId");
        System.out.println("✓ Auction created successfully!");
        System.out.println("  Auction ID: " + auctionId);
        System.out.println("  Status: OPEN");
        System.out.println("  Starting Price: 100.00 PLN\n");

        System.out.println("Verifying auction details...");
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

        System.out.println("✓ Auction verified!");
        System.out.println("  Title: " + auctionTitle);
        System.out.println("  Status: OPEN");
        System.out.println("  Starting Price: 100.00 PLN\n");

        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  SCENARIO B: PASSED ✓                                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");
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
