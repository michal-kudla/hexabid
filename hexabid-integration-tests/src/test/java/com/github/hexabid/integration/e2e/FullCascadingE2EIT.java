package com.github.hexabid.integration.e2e;

import com.github.hexabid.contract.client.model.*;
import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class FullCascadingE2EIT extends IntegrationTestBase {

    private static final String WS_URL = "ws://localhost:18080/hexabid/ws-auctions";
    private static final StandardWebSocketClient webSocketClient = new StandardWebSocketClient();

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

    @Nested
    @DisplayName("E2E1 - Full supply chain with pricing, wadium, and WebSocket error on bid")
    class IT_E2E1_FullSupplyChain {

        @Test
        @DisplayName("Should complete: Product -> Batch -> Instance -> Auction -> Wadium -> Price -> Refund + verify bid rejected (DRAFT)")
        void shouldCompleteFullSupplyChain() throws Exception {
            // Step 1: Register product
            CreateProductTypeRequest prodReq = productRequest("E2E wheat",
                    "Pszenica ozima dla E2E test", ProductTrackingStrategy.BATCH_TRACKED, "kg");
            ProductTypeResponse product = sellerProductsApi.createProductType(prodReq, API_VERSION);
            UUID productId = product.getProductId();
            assertThat(productId).isNotNull();

            // Step 2: Create batch
            CreateBatchRequest batchReq = new CreateBatchRequest();
            batchReq.setProductId(productId);
            batchReq.setName(unique("WHEAT-2026-E2E"));
            batchReq.setQuantity(batchQty("5000", "kg"));
            BatchResponse batch = sellerInventoryApi.createBatch(batchReq, API_VERSION);
            assertThat(batch.getBatchId()).isNotNull();

            // Step 3: Create inventory instance
            CreateInventoryInstanceRequest instReq = new CreateInventoryInstanceRequest();
            instReq.setProductId(productId);
            instReq.setBatchId(batch.getBatchId());
            instReq.setQuantity(instanceQty("1000", "kg"));
            InventoryInstanceResponse instance = sellerInventoryApi.createInventoryInstance(instReq, API_VERSION);
            assertThat(instance.getInstanceId()).isNotNull();
            assertThat(instance.getBatchId()).isEqualTo(batch.getBatchId());

            // Step 4: Create auction with pricing config
            CreateAuctionRequest auctionReq = auctionRequest(
                    "E2E full chain auction", "50000.00", pricingConfigImportedExcisableCar());
            AuctionResponse auction = sellerAuctionsApi.createAuction(auctionReq, API_VERSION);
            UUID auctionId = auction.getAuctionId();
            assertThat(auctionId).isNotNull();
            assertThat(auction.getStatus()).isEqualTo(AuctionStatus.DRAFT);
            assertThat(auction.getPricingConfig()).isNotNull();

            // Step 5: Verify auction visible in public browse
            assertThat(findAuctionInBrowse(auctionId, "E2E full chain")).isNotNull();

            // Step 6: Verify auction in seller's my-auctions
            assertThat(auctionExistsInMyAuctions(auctionId)).isTrue();

            // Step 7: Attempt bid via WebSocket - should be rejected (auction is DRAFT)
            StompSession buyerSession = connectAs(BUYER_USER, BUYER_PASS);

            CountDownLatch errorLatch = new CountDownLatch(1);
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
                    errorLatch.countDown();
                }
            });

            Thread.sleep(500);

            StompHeaders bidHeaders = new StompHeaders();
            bidHeaders.setDestination("/app/auctions/" + auctionId + "/bids");
            buyerSession.send(bidHeaders, Map.of("amount", "55000.00", "currency", "PLN"));

            boolean errorReceived = errorLatch.await(10, TimeUnit.SECONDS);
            assertThat(errorReceived).isTrue();
            assertThat(String.valueOf(errorMessage.get().get("reason"))).contains("NOT_IN_PROGRESS");

            // Step 8: Deposit wadium
            DepositWadiumRequest wadiumReq = new DepositWadiumRequest();
            wadiumReq.setAmount(pln("2500.00"));
            WadiumResponse wadium = buyerAuctionsApi.depositWadium(auctionId, wadiumReq, API_VERSION);
            assertThat(wadium.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);
            assertThat(wadium.getDeductibleOnWin()).isTrue();
            assertThat(wadium.getRefundableOnLoss()).isTrue();

            // Step 9: Get price breakdown
            AuctionPriceBreakdownResponse price = sellerAuctionsApi.getAuctionPrice(auctionId, API_VERSION);

            BigDecimal hammer = new BigDecimal(price.getHammerPrice().getAmount());
            BigDecimal netto = new BigDecimal(price.getNetto().getAmount());
            BigDecimal excise = new BigDecimal(price.getExcise().getAmount());
            BigDecimal customs = new BigDecimal(price.getCustomsDuty().getAmount());
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            BigDecimal totalDue = new BigDecimal(price.getTotalDue().getAmount());

            assertThat(hammer).isEqualByComparingTo(netto.add(new BigDecimal(price.getWadiumOffset().getAmount())));
            assertThat(totalDue).isEqualByComparingTo(netto.add(excise).add(customs).add(vat));
            assertThat(price.getAppliedRates().getVatRate()).isEqualTo("23%");

            // Step 10: Refund wadium
            RefundWadiumRequest refundReq = new RefundWadiumRequest();
            refundReq.setPartyId(auctionId);
            WadiumRefundResponse refund = buyerAuctionsApi.refundWadium(auctionId, refundReq, API_VERSION);
            assertThat(refund.getStatus()).isEqualTo(WadiumRefundResponse.StatusEnum.REFUNDED);
            assertThat(refund.getRefundAmount().getAmount()).isEqualTo("2500.00");

            buyerSession.disconnect();
        }
    }

    @Nested
    @DisplayName("E2E2 - Multiple bid rejections on DRAFT auction")
    class IT_E2E2_MultipleBidRejections {

        @Test
        @DisplayName("Should reject multiple bids on DRAFT auction with consistent error")
        void shouldRejectMultipleBidsOnDraftAuction() throws Exception {
            CreateAuctionRequest req = auctionRequest("E2E multi-rejection auction", "1000.00",
                    pricingConfigCar());
            AuctionResponse auction = sellerAuctionsApi.createAuction(req, API_VERSION);
            UUID auctionId = auction.getAuctionId();

            StompSession buyerSession = connectAs(BUYER_USER, BUYER_PASS);

            CountDownLatch latch = new CountDownLatch(2);
            java.util.concurrent.atomic.AtomicInteger errorCount = new java.util.concurrent.atomic.AtomicInteger(0);

            buyerSession.subscribe("/topic/auctions/" + auctionId + "/errors", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Map.class;
                }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    errorCount.incrementAndGet();
                    latch.countDown();
                }
            });

            Thread.sleep(500);

            StompHeaders bid1 = new StompHeaders();
            bid1.setDestination("/app/auctions/" + auctionId + "/bids");
            buyerSession.send(bid1, Map.of("amount", "1500.00", "currency", "PLN"));

            Thread.sleep(300);

            StompHeaders bid2 = new StompHeaders();
            bid2.setDestination("/app/auctions/" + auctionId + "/bids");
            buyerSession.send(bid2, Map.of("amount", "2000.00", "currency", "PLN"));

            boolean allReceived = latch.await(15, TimeUnit.SECONDS);
            assertThat(allReceived).isTrue();
            assertThat(errorCount.get()).isGreaterThanOrEqualTo(2);

            buyerSession.disconnect();
        }
    }

    @Nested
    @DisplayName("E2E3 - Complete inventory lifecycle")
    class IT_E2E3_InventoryLifecycle {

        @Test
        @DisplayName("Should complete: Product -> Batch -> Instance -> Verify -> Browse -> Filter")
        void shouldCompleteInventoryLifecycle() throws Exception {
            CreateProductTypeRequest batchReq = productRequest("E2E3 corn",
                    "Corn for full lifecycle", ProductTrackingStrategy.BATCH_TRACKED, "kg");
            UUID batchProductId = sellerProductsApi.createProductType(batchReq, API_VERSION).getProductId();

            CreateProductTypeRequest uniqueReq = productRequest("E2E3 car",
                    "Car for full lifecycle", ProductTrackingStrategy.UNIQUE, "pcs");
            UUID uniqueProductId = sellerProductsApi.createProductType(uniqueReq, API_VERSION).getProductId();

            CreateBatchRequest batchCreateReq = new CreateBatchRequest();
            batchCreateReq.setProductId(batchProductId);
            batchCreateReq.setName(unique("CORN-2026-E2E3"));
            batchCreateReq.setQuantity(batchQty("10000", "kg"));
            BatchResponse batch = sellerInventoryApi.createBatch(batchCreateReq, API_VERSION);

            CreateInventoryInstanceRequest batchedInstReq = new CreateInventoryInstanceRequest();
            batchedInstReq.setProductId(batchProductId);
            batchedInstReq.setBatchId(batch.getBatchId());
            batchedInstReq.setQuantity(instanceQty("500", "kg"));
            InventoryInstanceResponse batchedInstance = sellerInventoryApi.createInventoryInstance(batchedInstReq, API_VERSION);

            CreateInventoryInstanceRequest uniqueInstReq = new CreateInventoryInstanceRequest();
            uniqueInstReq.setProductId(uniqueProductId);
            uniqueInstReq.setSerialNumber("VIN-E2E3-TEST");
            uniqueInstReq.setQuantity(instanceQty("1", "pcs"));
            InventoryInstanceResponse uniqueInstance = sellerInventoryApi.createInventoryInstance(uniqueInstReq, API_VERSION);

            InventoryInstanceResponse fetchedBatched = sellerInventoryApi.getInventoryInstance(
                    batchedInstance.getInstanceId(), API_VERSION);
            assertThat(fetchedBatched.getBatchId()).isEqualTo(batch.getBatchId());

            InventoryInstanceResponse fetchedUnique = sellerInventoryApi.getInventoryInstance(
                    uniqueInstance.getInstanceId(), API_VERSION);
            assertThat(fetchedUnique.getSerialNumber()).isEqualTo("VIN-E2E3-TEST");

            InventoryInstanceListResponse batchFiltered = sellerInventoryApi.browseInventoryInstances(
                    API_VERSION, null, batchProductId, 50, null);
            assertThat(batchFiltered.getItems().stream()
                    .allMatch(i -> i.getProductId().equals(batchProductId))).isTrue();

            InventoryInstanceListResponse uniqueFiltered = sellerInventoryApi.browseInventoryInstances(
                    API_VERSION, null, uniqueProductId, 50, null);
            assertThat(uniqueFiltered.getItems().stream()
                    .allMatch(i -> i.getProductId().equals(uniqueProductId))).isTrue();
        }
    }

    @Nested
    @DisplayName("E2E4 - Pricing with all tax components")
    class IT_E2E4_FullPricing {

        @Test
        @DisplayName("Should calculate complete pricing: wadium + excise + customs + VAT")
        void shouldCalculateFullPricing() throws Exception {
            CreateAuctionRequest req = auctionRequest("E2E4 full pricing", "100000.00",
                    pricingConfigImportedExcisableCar());
            AuctionResponse auction = sellerAuctionsApi.createAuction(req, API_VERSION);

            DepositWadiumRequest wadiumReq = new DepositWadiumRequest();
            wadiumReq.setAmount(pln("10000.00"));
            buyerAuctionsApi.depositWadium(auction.getAuctionId(), wadiumReq, API_VERSION);

            AuctionPriceBreakdownResponse price = sellerAuctionsApi.getAuctionPrice(
                    auction.getAuctionId(), API_VERSION);

            BigDecimal hammer = new BigDecimal(price.getHammerPrice().getAmount());
            BigDecimal wadiumOffset = new BigDecimal(price.getWadiumOffset().getAmount());
            BigDecimal netto = new BigDecimal(price.getNetto().getAmount());
            BigDecimal excise = new BigDecimal(price.getExcise().getAmount());
            BigDecimal customs = new BigDecimal(price.getCustomsDuty().getAmount());
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            BigDecimal totalDue = new BigDecimal(price.getTotalDue().getAmount());

            assertThat(hammer).isEqualByComparingTo(netto.add(wadiumOffset));
            assertThat(excise).isGreaterThan(BigDecimal.ZERO);
            assertThat(customs).isGreaterThan(BigDecimal.ZERO);
            assertThat(vat).isGreaterThan(BigDecimal.ZERO);
            assertThat(totalDue).isEqualByComparingTo(netto.add(excise).add(customs).add(vat));

            assertThat(price.getAppliedRates()).isNotNull();
            assertThat(price.getAppliedRates().getVatRate()).isEqualTo("23%");
        }
    }
}
