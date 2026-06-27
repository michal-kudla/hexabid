package com.github.hexabid.integration.websocket;

import com.github.hexabid.contract.client.model.*;
import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketBiddingIT extends IntegrationTestBase {

    private static final String WS_URL = "ws://localhost:18080/hexabid/ws-auctions";

    private static StandardWebSocketClient webSocketClient;

    @BeforeAll
    static void setupWebSocketClient() {
        webSocketClient = new StandardWebSocketClient();
    }

    private StompSession connectAs(String username, String password) throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        WebSocketHttpHeaders wsHeaders = new WebSocketHttpHeaders();
        wsHeaders.add("Authorization", basicAuth(username, password));

        CompletableFuture<StompSession> future = stompClient.connectAsync(
                WS_URL, wsHeaders, new StompSessionHandlerAdapter() {
                    @Override
                    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                        exception.printStackTrace();
                    }
                });
        return future.get(10, TimeUnit.SECONDS);
    }

    private StompSession connectAnonymously() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        CompletableFuture<StompSession> future = stompClient.connectAsync(
                WS_URL, new StompSessionHandlerAdapter() {
                    @Override
                    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                        exception.printStackTrace();
                    }
                });
        return future.get(10, TimeUnit.SECONDS);
    }

    @Nested
    @DisplayName("WS1 - Authenticated bid on DRAFT auction rejected (not in progress)")
    class IT_WS1_BidOnDraftAuction {

        @Test
        @DisplayName("Should reject bid on DRAFT auction with AUCTION_NOT_IN_PROGRESS")
        void shouldRejectBidOnDraftAuction() throws Exception {
            StompSession sellerSession = connectAs(SELLER_USER, SELLER_PASS);
            StompSession buyerSession = connectAs(BUYER_USER, BUYER_PASS);

            AuctionResponse auction = sellerAuctionsApi.createAuction(
                    auctionRequest("WS1 draft auction bid", "1000.00"), API_VERSION);
            UUID auctionId = auction.getAuctionId();

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Map<String, Object>> errorMessage = new AtomicReference<>();

            buyerSession.subscribe("/topic/auctions/" + auctionId + "/errors", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Map.class;
                }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) payload;
                    errorMessage.set(map);
                    latch.countDown();
                }
            });

            Thread.sleep(500);

            StompHeaders bidHeaders = new StompHeaders();
            bidHeaders.setDestination("/app/auctions/" + auctionId + "/bids");
            buyerSession.send(bidHeaders, Map.of("amount", "1500.00", "currency", "PLN"));

            boolean received = latch.await(10, TimeUnit.SECONDS);
            assertThat(received).isTrue();

            Map<String, Object> msg = errorMessage.get();
            assertThat(msg).isNotNull();
            assertThat(String.valueOf(msg.get("reason"))).contains("NOT_IN_PROGRESS");

            sellerSession.disconnect();
            buyerSession.disconnect();
        }
    }

    @Nested
    @DisplayName("WS2 - Unauthenticated user cannot bid")
    class IT_WS2_UnauthenticatedBid {

        @Test
        @DisplayName("Should reject bid from unauthenticated WebSocket session")
        void shouldRejectUnauthenticatedBid() throws Exception {
            StompSession anonSession = connectAnonymously();

            AuctionResponse auction = sellerAuctionsApi.createAuction(
                    auctionRequest("WS2 unauth bid auction", "1000.00"), API_VERSION);
            UUID auctionId = auction.getAuctionId();

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Map<String, Object>> errorMessage = new AtomicReference<>();

            anonSession.subscribe("/topic/auctions/" + auctionId + "/errors", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Map.class;
                }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) payload;
                    errorMessage.set(map);
                    latch.countDown();
                }
            });

            Thread.sleep(500);

            StompHeaders bidHeaders = new StompHeaders();
            bidHeaders.setDestination("/app/auctions/" + auctionId + "/bids");
            anonSession.send(bidHeaders, Map.of("amount", "2000.00", "currency", "PLN"));

            boolean received = latch.await(10, TimeUnit.SECONDS);
            assertThat(received).isTrue();

            Map<String, Object> msg = errorMessage.get();
            assertThat(msg).isNotNull();
            assertThat(String.valueOf(msg.get("reason"))).isEqualTo("UNAUTHENTICATED");

            anonSession.disconnect();
        }
    }

    @Nested
    @DisplayName("WS3 - Seller cannot bid on own auction")
    class IT_WS3_SellerCannotBid {

        @Test
        @DisplayName("Should reject when seller bids on own auction (DRAFT -> NOT_IN_PROGRESS)")
        void shouldRejectSellerBiddingOnOwnAuction() throws Exception {
            StompSession sellerSession = connectAs(SELLER_USER, SELLER_PASS);

            AuctionResponse auction = sellerAuctionsApi.createAuction(
                    auctionRequest("WS3 seller own bid auction", "1000.00"), API_VERSION);
            UUID auctionId = auction.getAuctionId();

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Map<String, Object>> errorMessage = new AtomicReference<>();

            sellerSession.subscribe("/topic/auctions/" + auctionId + "/errors", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Map.class;
                }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) payload;
                    errorMessage.set(map);
                    latch.countDown();
                }
            });

            Thread.sleep(500);

            StompHeaders bidHeaders = new StompHeaders();
            bidHeaders.setDestination("/app/auctions/" + auctionId + "/bids");
            sellerSession.send(bidHeaders, Map.of("amount", "2000.00", "currency", "PLN"));

            boolean received = latch.await(10, TimeUnit.SECONDS);
            assertThat(received).isTrue();

            Map<String, Object> msg = errorMessage.get();
            assertThat(msg).isNotNull();
            assertThat(String.valueOf(msg.get("reason"))).isNotBlank();

            sellerSession.disconnect();
        }
    }

    @Nested
    @DisplayName("WS4 - Bid on non-existent auction")
    class IT_WS4_BidOnNonExistentAuction {

        @Test
        @DisplayName("Should reject bid on non-existent auction")
        void shouldRejectBidOnNonExistentAuction() throws Exception {
            StompSession buyerSession = connectAs(BUYER_USER, BUYER_PASS);
            UUID randomAuctionId = UUID.randomUUID();

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Map<String, Object>> errorMessage = new AtomicReference<>();

            buyerSession.subscribe("/topic/auctions/" + randomAuctionId + "/errors", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Map.class;
                }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) payload;
                    errorMessage.set(map);
                    latch.countDown();
                }
            });

            Thread.sleep(500);

            StompHeaders bidHeaders = new StompHeaders();
            bidHeaders.setDestination("/app/auctions/" + randomAuctionId + "/bids");
            buyerSession.send(bidHeaders, Map.of("amount", "1500.00", "currency", "PLN"));

            boolean received = latch.await(10, TimeUnit.SECONDS);
            assertThat(received).isTrue();

            Map<String, Object> msg = errorMessage.get();
            assertThat(msg).isNotNull();
            assertThat(String.valueOf(msg.get("reason"))).contains("NOT_FOUND");

            buyerSession.disconnect();
        }
    }

    @Nested
    @DisplayName("WS5 - Subscribe to auction bids channel")
    class IT_WS5_SubscribeToBidsChannel {

        @Test
        @DisplayName("Should be able to subscribe to /topic/auctions/{id}/bids without error")
        void shouldSubscribeToBidsChannel() throws Exception {
            StompSession buyerSession = connectAs(BUYER_USER, BUYER_PASS);

            AuctionResponse auction = sellerAuctionsApi.createAuction(
                    auctionRequest("WS5 subscription auction", "1000.00"), API_VERSION);
            UUID auctionId = auction.getAuctionId();

            CountDownLatch subscribeLatch = new CountDownLatch(1);
            AtomicReference<Throwable> subscriptionError = new AtomicReference<>();

            buyerSession.subscribe("/topic/auctions/" + auctionId + "/bids", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Map.class;
                }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    subscribeLatch.countDown();
                }
            });

            Thread.sleep(500);
            assertThat(subscriptionError.get()).isNull();

            buyerSession.disconnect();
        }
    }

    @Nested
    @DisplayName("WS6 - Subscribe to auction errors channel")
    class IT_WS6_SubscribeToErrorsChannel {

        @Test
        @DisplayName("Should be able to subscribe to /topic/auctions/{id}/errors and receive rejection")
        void shouldSubscribeToErrorsChannel() throws Exception {
            StompSession buyerSession = connectAs(BUYER_USER, BUYER_PASS);

            AuctionResponse auction = sellerAuctionsApi.createAuction(
                    auctionRequest("WS6 errors subscription auction", "1000.00"), API_VERSION);
            UUID auctionId = auction.getAuctionId();

            CountDownLatch errorLatch = new CountDownLatch(1);
            AtomicReference<Map<String, Object>> receivedError = new AtomicReference<>();

            buyerSession.subscribe("/topic/auctions/" + auctionId + "/errors", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Map.class;
                }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) payload;
                    receivedError.set(map);
                    errorLatch.countDown();
                }
            });

            Thread.sleep(500);

            StompHeaders bidHeaders = new StompHeaders();
            bidHeaders.setDestination("/app/auctions/" + auctionId + "/bids");
            buyerSession.send(bidHeaders, Map.of("amount", "100.00", "currency", "PLN"));

            boolean received = errorLatch.await(10, TimeUnit.SECONDS);
            assertThat(received).isTrue();

            Map<String, Object> msg = receivedError.get();
            assertThat(msg).isNotNull();
            assertThat(String.valueOf(msg.get("reason"))).isNotBlank();

            buyerSession.disconnect();
        }
    }

    @Nested
    @DisplayName("WS7 - Subscribe to auction events channel")
    class IT_WS7_AuctionEventsSubscription {

        @Test
        @DisplayName("Should be able to subscribe to /topic/auctions/{id}/events without error")
        void shouldSubscribeToEventsChannel() throws Exception {
            StompSession buyerSession = connectAs(BUYER_USER, BUYER_PASS);

            AuctionResponse auction = sellerAuctionsApi.createAuction(
                    auctionRequest("WS7 events subscription auction", "1000.00"), API_VERSION);
            UUID auctionId = auction.getAuctionId();

            AtomicReference<Throwable> subscriptionError = new AtomicReference<>();

            buyerSession.subscribe("/topic/auctions/" + auctionId + "/events", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Map.class;
                }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                }
            });

            Thread.sleep(500);
            assertThat(subscriptionError.get()).isNull();

            buyerSession.disconnect();
        }
    }
}
