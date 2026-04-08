package com.acme.auctions.integration.flow;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = ProductBatchInstanceAuctionScenariosIT.TestIntegrationApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ProductBatchInstanceAuctionScenariosIT {

    private static final String API_VERSION = "1";

    @LocalServerPort
    private int port;

    @BeforeEach
    void setupRestAssured() {
        RestAssured.baseURI = "http://127.0.0.1";
        RestAssured.port = port;
    }

    @Test
    void scenarioA_uniqueProductOnAuction() {
        String sellerSessionId = loginAndGetSessionId("user", "password");

        UUID productId = createProductType(sellerSessionId, "Jan Kowalski's Car", 
            "Seat Leon 1999 TDI", "UNIQUE", "pcs");

        String auctionTitle = "Unikalny samochód - Seat Leon 1999";
        OffsetDateTime endsAt = OffsetDateTime.now().plusHours(4).truncatedTo(ChronoUnit.SECONDS);

        ExtractableResponse<Response> createAuctionResponse = given()
                .contentType(ContentType.JSON)
                .header("X-API-Version", API_VERSION)
                .cookie("JSESSIONID", sellerSessionId)
                .body(Map.of(
                        "title", auctionTitle,
                        "startingPrice", Map.of(
                                "amount", "5000.00",
                                "currency", "PLN"
                        ),
                        "endsAt", endsAt.toString()
                ))
                .when()
                .post("/api/auctions")
                .then()
                .statusCode(201)
                .extract();

        String auctionId = createAuctionResponse.path("auctionId");
        assertThat(auctionId).isNotBlank();

        given()
                .header("X-API-Version", API_VERSION)
                .when()
                .get("/api/auctions/{auctionId}", auctionId)
                .then()
                .statusCode(200)
                .body("title", org.hamcrest.Matchers.equalTo(auctionTitle))
                .body("status", org.hamcrest.Matchers.equalTo("OPEN"))
                .body("buyNowPrice", org.hamcrest.Matchers.nullValue());
    }

    @Test
    void scenarioB_divisibleBatchWithBuyNow() {
        String sellerSessionId = loginAndGetSessionId("user", "password");

        UUID productId = createProductType(sellerSessionId, 
            "Ziemniaki", "Świeże ziemniaki z polski", "BATCH_TRACKED", "kg");

        UUID batchId = createBatch(sellerSessionId, productId, 
            "POTATO-2024-WARSZAWA-001", new BigDecimal("2000"), "kg");

        for (int i = 0; i < 20; i++) {
            createInventoryInstance(sellerSessionId, productId, batchId, 
                new BigDecimal("100"), "kg");
        }

        String lotTitle = "20 worków ziemniaków po 100 kg";
        UUID lotId = createLot(sellerSessionId, lotTitle, "Świeże ziemniaki z partii", 
            null, "DIVISIBLE", new BigDecimal("150"), "PLN");

        String auctionTitle = "Ziemniaki - sprzedaż z kup teraz";
        OffsetDateTime endsAt = OffsetDateTime.now().plusHours(2).truncatedTo(ChronoUnit.SECONDS);

        ExtractableResponse<Response> createAuctionResponse = given()
                .contentType(ContentType.JSON)
                .header("X-API-Version", API_VERSION)
                .cookie("JSESSIONID", sellerSessionId)
                .body(Map.of(
                        "title", auctionTitle,
                        "startingPrice", Map.of(
                                "amount", "100.00",
                                "currency", "PLN"
                        ),
                        "endsAt", endsAt.toString(),
                        "lotId", lotId,
                        "buyNowPrice", Map.of(
                                "amount", "250.00",
                                "currency", "PLN"
                        )
                ))
                .when()
                .post("/api/auctions")
                .then()
                .statusCode(201)
                .extract();

        String auctionId = createAuctionResponse.path("auctionId");
        assertThat(auctionId).isNotBlank();

        given()
                .header("X-API-Version", API_VERSION)
                .when()
                .get("/api/auctions/{auctionId}", auctionId)
                .then()
                .statusCode(200)
                .body("title", org.hamcrest.Matchers.equalTo(auctionTitle))
                .body("buyNowPrice.amount", org.hamcrest.Matchers.equalTo("250.00"))
                .body("buyNowPrice.currency", org.hamcrest.Matchers.equalTo("PLN"));
    }

    private UUID createProductType(String sessionId, String name, String description, 
                                   String trackingStrategy, String preferredUnit) {
        ExtractableResponse<Response> response = given()
                .contentType(ContentType.JSON)
                .header("X-API-Version", API_VERSION)
                .cookie("JSESSIONID", sessionId)
                .body(Map.of(
                        "name", name,
                        "description", description,
                        "trackingStrategy", trackingStrategy,
                        "preferredUnit", preferredUnit
                ))
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract();

        return UUID.fromString(response.path("productId"));
    }

    private UUID createBatch(String sessionId, UUID productId, String name, 
                             BigDecimal quantity, String unit) {
        ExtractableResponse<Response> response = given()
                .contentType(ContentType.JSON)
                .header("X-API-Version", API_VERSION)
                .cookie("JSESSIONID", sessionId)
                .body(Map.of(
                        "productId", productId.toString(),
                        "name", name,
                        "quantity", Map.of(
                                "amount", quantity.toPlainString(),
                                "unit", unit
                        )
                ))
                .when()
                .post("/api/batches")
                .then()
                .statusCode(201)
                .extract();

        return UUID.fromString(response.path("batchId"));
    }

    private UUID createInventoryInstance(String sessionId, UUID productId, 
                                         UUID batchId, BigDecimal quantity, String unit) {
        ExtractableResponse<Response> response = given()
                .contentType(ContentType.JSON)
                .header("X-API-Version", API_VERSION)
                .cookie("JSESSIONID", sessionId)
                .body(Map.of(
                        "productId", productId.toString(),
                        "batchId", batchId.toString(),
                        "quantity", Map.of(
                                "amount", quantity.toPlainString(),
                                "unit", unit
                        )
                ))
                .when()
                .post("/api/inventory/instances")
                .then()
                .statusCode(201)
                .extract();

        return UUID.fromString(response.path("instanceId"));
    }

    private UUID createLot(String sessionId, String title, String description, 
                          UUID inventoryEntryId, String sellingMode, 
                          BigDecimal reserveAmount, String currency) {
        var requestBody = Map.of(
                "title", title,
                "description", description,
                "sellingMode", sellingMode
        );
        
        if (inventoryEntryId != null) {
            ((java.util.Map<String, Object>) requestBody).put("inventoryEntryId", inventoryEntryId.toString());
        }
        if (reserveAmount != null) {
            ((java.util.Map<String, Object>) requestBody).put("reservePrice", 
                Map.of("amount", reserveAmount.toPlainString(), "currency", currency));
        }

        ExtractableResponse<Response> response = given()
                .contentType(ContentType.JSON)
                .header("X-API-Version", API_VERSION)
                .cookie("JSESSIONID", sessionId)
                .body(requestBody)
                .when()
                .post("/api/lots")
                .then()
                .statusCode(201)
                .extract();

        return UUID.fromString(response.path("lotId"));
    }

    private String loginAndGetSessionId(String username, String password) {
        return given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", username)
                .formParam("password", password)
                .redirects().follow(false)
                .when()
                .post("/login")
                .then()
                .statusCode(org.hamcrest.Matchers.isOneOf(302, 303))
                .extract()
                .cookie("JSESSIONID");
    }

    @EnableScheduling
    @EntityScan(basePackages = "com.acme.auctions.adapter.out.db")
    @EnableJpaRepositories(basePackages = "com.acme.auctions.adapter.out.db")
    @SpringBootApplication(scanBasePackages = "com.acme.auctions")
    static class TestIntegrationApplication {
    }
}