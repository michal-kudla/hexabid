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

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = BasicCommerceFlowApiIT.TestIntegrationApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class BasicCommerceFlowApiIT {

    private static final String API_VERSION = "1";

    @LocalServerPort
    private int port;

    @BeforeEach
    void setupRestAssured() {
        RestAssured.baseURI = "http://127.0.0.1";
        RestAssured.port = port;
    }

    @Test
    void shouldCoverBaselineBackendFlowUsingRealHttpEndpoints() {
        String sellerSessionId = loginAndGetSessionId("user", "password");

        String auctionTitle = "IT flow " + UUID.randomUUID();
        OffsetDateTime endsAt = OffsetDateTime.now().plusHours(4).truncatedTo(ChronoUnit.SECONDS);

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
                .body("status", org.hamcrest.Matchers.equalTo("OPEN"));

        given()
                .header("X-API-Version", API_VERSION)
                .cookie("JSESSIONID", sellerSessionId)
                .when()
                .get("/api/me/auctions")
                .then()
                .statusCode(200)
                .body("items.auctionId", org.hamcrest.Matchers.hasItem(auctionId));

        String buyerSessionId = loginAndGetSessionId("admin", "password");

        given()
                .header("X-API-Version", API_VERSION)
                .cookie("JSESSIONID", buyerSessionId)
                .when()
                .get("/api/payments/gateways")
                .then()
                .statusCode(200)
                .body("size()", org.hamcrest.Matchers.greaterThan(0));
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
