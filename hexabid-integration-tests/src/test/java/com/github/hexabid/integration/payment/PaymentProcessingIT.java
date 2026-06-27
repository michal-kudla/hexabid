package com.github.hexabid.integration.payment;

import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentProcessingIT extends IntegrationTestBase {

    @Nested
    @DisplayName("PAY1 - List payment gateways")
    class IT_PAY1_ListGateways {

        @Test
        @DisplayName("Should return at least LOCAL gateway")
        void shouldReturnLocalGateway() throws Exception {
            String body = getDiscoveryEndpoint("/api/payments/gateways", SELLER_USER, SELLER_PASS);

            assertThat(body).contains("LOCAL");
            assertThat(body).contains("Lokalna Bramka");
            assertThat(body).contains("gatewayUrl");
        }
    }

    @Nested
    @DisplayName("PAY2 - LOCAL gateway URL format")
    class IT_PAY2_LocalGatewayUrl {

        @Test
        @DisplayName("LOCAL gateway URL should point to /api/payments/initiate")
        void shouldHaveCorrectGatewayUrl() throws Exception {
            String body = getDiscoveryEndpoint("/api/payments/gateways", SELLER_USER, SELLER_PASS);

            assertThat(body).contains("/api/payments/initiate?gatewayId=LOCAL");
        }
    }

    @Nested
    @DisplayName("PAY3 - Payment gateways require authentication")
    class IT_PAY3_GatewaysAuth {

        @Test
        @DisplayName("Should require authentication for payment gateways endpoint")
        void shouldRequireAuth() throws Exception {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/payments/gateways"))
                    .header("X-API-Version", API_VERSION)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(401);
        }
    }

    @Nested
    @DisplayName("PAY4 - Invalid gateway ID returns error")
    class IT_PAY4_InvalidGatewayId {

        @Test
        @DisplayName("Should return 404 or 400 for non-existent gateway ID")
        void shouldReturnErrorForInvalidGateway() throws Exception {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/payments/initiate?gatewayId=NONEXISTENT"))
                    .header("X-API-Version", API_VERSION)
                    .header("Authorization", basicAuth(BUYER_USER, BUYER_PASS))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isIn(400, 404, 501);
        }
    }
}
