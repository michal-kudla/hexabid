package com.github.hexabid.integration.flow;

import com.github.hexabid.contract.client.model.*;
import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test for the complete supply chain.
 *
 * <p>This class documents the primary business flow of the Hexabid platform:
 * from product registration all the way to pricing, wadium deposit, and refund.
 * Each nested class represents a major phase in the supply chain.</p>
 *
 * <h3>Full supply chain flow</h3>
 * <pre>
 *   1. ProductType    (M02 - what is it?)
 *   2. Batch          (M06 - which production lot?)
 *   3. InventoryInst  (M06 - how much do we have?)
 *   4. Lot            (M03 - how do we sell it?)
 *   5. Auction        (M03 - when and for how much?)
 *   6. Pricing        (M03 - what are the taxes and fees?)
 *   7. Wadium         (M03 - bid security deposit)
 *   8. Refund         (M03 - return of bid security)
 * </pre>
 */
class FullSupplyChainFlowIT extends IntegrationTestBase {

    @Nested
    @DisplayName("S1 - Register product types in the catalog")
    class IT_S1_RegisterProductTypes {

        @Test
        @DisplayName("Should register a BATCH_TRACKED product and a UNIQUE product")
        void shouldRegisterProductTypes() throws Exception {
            CreateProductTypeRequest cornReq = productRequest(
                    "Supply chain corn",
                    "Kukurydza na ziarno do testu supply chain",
                    ProductTrackingStrategy.BATCH_TRACKED,
                    "kg"
            );
            ProductTypeResponse corn = sellerProductsApi.createProductType(cornReq, API_VERSION);
            assertThat(corn.getProductId()).isNotNull();
            assertThat(corn.getTrackingStrategy()).isEqualTo(ProductTrackingStrategy.BATCH_TRACKED);

            CreateProductTypeRequest carReq = productRequest(
                    "Supply chain car",
                    "VW Golf 2020 TDI for supply chain test",
                    ProductTrackingStrategy.UNIQUE,
                    "pcs"
            );
            ProductTypeResponse car = sellerProductsApi.createProductType(carReq, API_VERSION);
            assertThat(car.getProductId()).isNotNull();
            assertThat(car.getTrackingStrategy()).isEqualTo(ProductTrackingStrategy.UNIQUE);
        }
    }

    @Nested
    @DisplayName("S2 - Create batch and inventory instances")
    class IT_S2_CreateBatchAndInstances {

        @Test
        @DisplayName("Should create batch from product, then instances from batch")
        void shouldCreateBatchAndInstances() throws Exception {
            CreateProductTypeRequest req = productRequest(
                    "S2 batch product",
                    "Product for batch+instance flow",
                    ProductTrackingStrategy.BATCH_TRACKED,
                    "kg"
            );
            UUID productId = sellerProductsApi.createProductType(req, API_VERSION).getProductId();

            CreateBatchRequest batchReq = new CreateBatchRequest();
            batchReq.setProductId(productId);
            batchReq.setName(unique("S2-BATCH-001"));
            batchReq.setQuantity(batchQty("5000", "kg"));
            BatchResponse batch = sellerInventoryApi.createBatch(batchReq, API_VERSION);
            assertThat(batch.getBatchId()).isNotNull();
            assertThat(batch.getProductId()).isEqualTo(productId);

            CreateInventoryInstanceRequest instReq = new CreateInventoryInstanceRequest();
            instReq.setProductId(productId);
            instReq.setBatchId(batch.getBatchId());
            instReq.setQuantity(instanceQty("500", "kg"));
            InventoryInstanceResponse instance = sellerInventoryApi.createInventoryInstance(instReq, API_VERSION);
            assertThat(instance.getInstanceId()).isNotNull();
            assertThat(instance.getBatchId()).isEqualTo(batch.getBatchId());

            InventoryInstanceResponse verified = sellerInventoryApi.getInventoryInstance(
                    instance.getInstanceId(), API_VERSION);
            assertThat(verified.getProductId()).isEqualTo(productId);
            assertThat(verified.getQuantity().getAmount()).isEqualTo("500");
        }
    }

    @Nested
    @DisplayName("S3 - Browse lots (endpoint not yet implemented)")
    class IT_S3_BrowseLots {

        @Test
        @DisplayName("Should return 501 for browse lots (not implemented)")
        void shouldBrowseLots() throws Exception {
            org.assertj.core.api.Assertions.assertThatThrownBy(
                            () -> sellerAuctionsApi.browseLots(API_VERSION, null, null, null))
                    .isInstanceOf(com.github.hexabid.contract.client.ApiException.class)
                    .satisfies(ex -> {
                        com.github.hexabid.contract.client.ApiException apiEx =
                                (com.github.hexabid.contract.client.ApiException) ex;
                        org.assertj.core.api.Assertions.assertThat(apiEx.getCode()).isEqualTo(501);
                    });
        }
    }

    @Nested
    @DisplayName("S4 - Create auction and verify pricing")
    class IT_S4_AuctionWithPricing {

        @Test
        @DisplayName("Should create auction and get price breakdown with all tax components")
        void shouldCreateAuctionAndGetFullPricing() throws Exception {
            CreateAuctionRequest auctionReq = auctionRequest("S4 full pricing auction", "100000.00",
                    pricingConfigImportedExcisableCar());
            AuctionResponse auction = sellerAuctionsApi.createAuction(auctionReq, API_VERSION);

            assertThat(auction.getPricingConfig()).isNotNull();

            AuctionPriceBreakdownResponse price = sellerAuctionsApi.getAuctionPrice(
                    auction.getAuctionId(), API_VERSION);

            BigDecimal hammer = new BigDecimal(price.getHammerPrice().getAmount());
            BigDecimal wadium = new BigDecimal(price.getWadiumOffset().getAmount());
            BigDecimal netto = new BigDecimal(price.getNetto().getAmount());
            BigDecimal excise = new BigDecimal(price.getExcise().getAmount());
            BigDecimal customs = new BigDecimal(price.getCustomsDuty().getAmount());
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            BigDecimal totalDue = new BigDecimal(price.getTotalDue().getAmount());

            assertThat(hammer).isEqualByComparingTo(netto.add(wadium));
            assertThat(totalDue).isEqualByComparingTo(netto.add(excise).add(customs).add(vat));
            assertThat(price.getAppliedRates().getVatRate()).isEqualTo("23%");
        }
    }

    @Nested
    @DisplayName("S5 - Wadium deposit and refund lifecycle")
    class IT_S5_WadiumLifecycle {

        @Test
        @DisplayName("Should deposit wadium, verify status, then refund it")
        void shouldDepositAndRefundWadium() throws Exception {
            AuctionResponse auction = sellerAuctionsApi.createAuction(
                    auctionRequest("S5 wadium auction", "20000.00", pricingConfigCar()), API_VERSION);

            DepositWadiumRequest depositReq = new DepositWadiumRequest();
            depositReq.setAmount(pln("1000.00"));
            WadiumResponse deposit = buyerAuctionsApi.depositWadium(
                    auction.getAuctionId(), depositReq, API_VERSION);

            assertThat(deposit.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);
            assertThat(deposit.getAuctionId()).isEqualTo(auction.getAuctionId());
            assertThat(deposit.getAmount().getAmount()).isEqualTo("1000.00");
            assertThat(deposit.getRefundableOnLoss()).isTrue();
            assertThat(deposit.getDeductibleOnWin()).isTrue();

            RefundWadiumRequest refundReq = new RefundWadiumRequest();
            refundReq.setPartyId(auction.getAuctionId());
            WadiumRefundResponse refund = buyerAuctionsApi.refundWadium(
                    auction.getAuctionId(), refundReq, API_VERSION);

            assertThat(refund.getStatus()).isEqualTo(WadiumRefundResponse.StatusEnum.REFUNDED);
            assertThat(refund.getRefundAmount().getAmount()).isEqualTo("1000.00");
            assertThat(refund.getAuctionId()).isEqualTo(auction.getAuctionId());
        }
    }

    /**
     * The grand scenario: walks the supply chain from product to wadium refund.
     *
     * <p>Steps: Product -> Batch -> Instance -> Auction -> Pricing -> Wadium -> Refund</p>
     *
     * <p>Note: The Lot step (bridge between inventory and auction) is skipped because
     * the createLot endpoint returns 501 (not yet implemented). Once implemented,
     * a lot creation step should be inserted between instance and auction.</p>
     */
    @Nested
    @DisplayName("S6 - Grand scenario: complete supply chain Product to Refund")
    class IT_S6_GrandSupplyChain {

        @Test
        @DisplayName("Should complete the full supply chain from product registration to wadium refund")
        void shouldCompleteFullSupplyChain() throws Exception {
            // Step 1: Product
            CreateProductTypeRequest prodReq = productRequest(
                    "Grand supply chain wheat",
                    "Pszenica ozima, klasa A, supply chain test",
                    ProductTrackingStrategy.BATCH_TRACKED,
                    "kg"
            );
            ProductTypeResponse product = sellerProductsApi.createProductType(prodReq, API_VERSION);
            UUID productId = product.getProductId();
            assertThat(productId).isNotNull();

            // Step 2: Batch
            CreateBatchRequest batchReq = new CreateBatchRequest();
            batchReq.setProductId(productId);
            batchReq.setName(unique("WHEAT-2026-POLAND-GRAND"));
            batchReq.setQuantity(batchQty("10000", "kg"));
            BatchResponse batch = sellerInventoryApi.createBatch(batchReq, API_VERSION);
            UUID batchId = batch.getBatchId();
            assertThat(batchId).isNotNull();
            assertThat(batch.getProductId()).isEqualTo(productId);

            // Step 3: Inventory instance
            CreateInventoryInstanceRequest instReq = new CreateInventoryInstanceRequest();
            instReq.setProductId(productId);
            instReq.setBatchId(batchId);
            instReq.setQuantity(instanceQty("2000", "kg"));
            InventoryInstanceResponse instance = sellerInventoryApi.createInventoryInstance(instReq, API_VERSION);
            UUID instanceId = instance.getInstanceId();
            assertThat(instanceId).isNotNull();
            assertThat(instance.getBatchId()).isEqualTo(batchId);

            // Step 4: Auction (without lot — createLot is 501)
            CreateAuctionRequest auctionReq = auctionRequest(
                    "Grand supply chain auction", "50000.00", pricingConfigImportedFlour());
            AuctionResponse auction = sellerAuctionsApi.createAuction(auctionReq, API_VERSION);
            UUID auctionId = auction.getAuctionId();
            assertThat(auctionId).isNotNull();
            assertThat(auction.getStatus()).isEqualTo(AuctionStatus.DRAFT);

            // Step 5: Price breakdown
            AuctionPriceBreakdownResponse price = sellerAuctionsApi.getAuctionPrice(auctionId, API_VERSION);
            assertThat(price.getHammerPrice().getAmount()).isEqualTo("50000.00");

            BigDecimal netto = new BigDecimal(price.getNetto().getAmount());
            BigDecimal excise = new BigDecimal(price.getExcise().getAmount());
            BigDecimal customs = new BigDecimal(price.getCustomsDuty().getAmount());
            BigDecimal vat = new BigDecimal(price.getVat().getAmount());
            BigDecimal totalDue = new BigDecimal(price.getTotalDue().getAmount());

            assertThat(excise).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(customs).isGreaterThan(BigDecimal.ZERO);
            assertThat(totalDue).isEqualByComparingTo(netto.add(excise).add(customs).add(vat));

            // Step 6: Wadium deposit
            DepositWadiumRequest wadiumReq = new DepositWadiumRequest();
            wadiumReq.setAmount(pln("2500.00"));
            WadiumResponse wadium = buyerAuctionsApi.depositWadium(auctionId, wadiumReq, API_VERSION);
            assertThat(wadium.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);
            assertThat(wadium.getDeductibleOnWin()).isTrue();
            assertThat(wadium.getRefundableOnLoss()).isTrue();

            // Step 7: Wadium refund
            RefundWadiumRequest refundReq = new RefundWadiumRequest();
            refundReq.setPartyId(auctionId);
            WadiumRefundResponse refund = buyerAuctionsApi.refundWadium(auctionId, refundReq, API_VERSION);
            assertThat(refund.getStatus()).isEqualTo(WadiumRefundResponse.StatusEnum.REFUNDED);
            assertThat(refund.getRefundAmount().getAmount()).isEqualTo("2500.00");
        }
    }

    private UUID createUniqueInstance() throws Exception {
        CreateProductTypeRequest prodReq = productRequest(
                "S3 unique product",
                "Unique product for lot test",
                ProductTrackingStrategy.UNIQUE,
                "pcs"
        );
        UUID productId = sellerProductsApi.createProductType(prodReq, API_VERSION).getProductId();

        CreateInventoryInstanceRequest instReq = new CreateInventoryInstanceRequest();
        instReq.setProductId(productId);
        instReq.setSerialNumber("VIN-S3-LOT-TEST");
        instReq.setQuantity(instanceQty("1", "pcs"));
        return sellerInventoryApi.createInventoryInstance(instReq, API_VERSION).getInstanceId();
    }

    private UUID createBatchInstance() throws Exception {
        CreateProductTypeRequest prodReq = productRequest(
                "S3 batch product",
                "Batch-tracked product for lot test",
                ProductTrackingStrategy.BATCH_TRACKED,
                "kg"
        );
        UUID productId = sellerProductsApi.createProductType(prodReq, API_VERSION).getProductId();

        CreateInventoryInstanceRequest instReq = new CreateInventoryInstanceRequest();
        instReq.setProductId(productId);
        instReq.setQuantity(instanceQty("2000", "kg"));
        return sellerInventoryApi.createInventoryInstance(instReq, API_VERSION).getInstanceId();
    }
}
