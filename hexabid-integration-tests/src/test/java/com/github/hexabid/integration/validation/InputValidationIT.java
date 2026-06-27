package com.github.hexabid.integration.validation;

import com.github.hexabid.contract.client.ApiException;
import com.github.hexabid.contract.client.api.AuctionsApi;
import com.github.hexabid.contract.client.api.InventoryApi;
import com.github.hexabid.contract.client.api.ProductsApi;
import com.github.hexabid.contract.client.model.*;
import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputValidationIT extends IntegrationTestBase {

    @Nested
    @DisplayName("VAL1 - Create auction with past endsAt returns 400")
    class IT_VAL1_PastEndsAt {

        @Test
        @DisplayName("Should reject auction with end time in the past")
        void shouldRejectPastEndsAt() throws Exception {
            CreateAuctionRequest req = new CreateAuctionRequest();
            req.setTitle(unique("VAL1 past ends auction"));
            req.setStartingPrice(pln("1000.00"));
            req.setEndsAt(OffsetDateTime.now().minusHours(1));

            assertThatThrownBy(() -> sellerAuctionsApi.createAuction(req, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(400));
        }
    }

    @Nested
    @DisplayName("VAL2 - Create auction with zero price returns 400")
    class IT_VAL2_ZeroPrice {

        @Test
        @DisplayName("Should reject auction with zero starting price")
        void shouldRejectZeroPrice() throws Exception {
            CreateAuctionRequest req = new CreateAuctionRequest();
            req.setTitle(unique("VAL2 zero price auction"));
            req.setStartingPrice(pln("0.00"));
            req.setEndsAt(OffsetDateTime.now().plusHours(4));

            assertThatThrownBy(() -> sellerAuctionsApi.createAuction(req, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isIn(400, 500));
        }
    }

    @Nested
    @DisplayName("VAL3 - Create auction with negative price returns 400")
    class IT_VAL3_NegativePrice {

        @Test
        @DisplayName("Should reject auction with negative starting price")
        void shouldRejectNegativePrice() throws Exception {
            CreateAuctionRequest req = new CreateAuctionRequest();
            req.setTitle(unique("VAL3 negative price auction"));
            req.setStartingPrice(pln("-100.00"));
            req.setEndsAt(OffsetDateTime.now().plusHours(4));

            assertThatThrownBy(() -> sellerAuctionsApi.createAuction(req, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isIn(400, 500));
        }
    }

    @Nested
    @DisplayName("VAL4 - Create batch for non-existent product returns 400")
    class IT_VAL4_BatchNonExistentProduct {

        @Test
        @DisplayName("Should reject batch creation with non-existent productId")
        void shouldRejectBatchForNonExistentProduct() throws Exception {
            CreateBatchRequest req = new CreateBatchRequest();
            req.setProductId(UUID.randomUUID());
            req.setName(unique("VAL4 ghost batch"));
            req.setQuantity(batchQty("500", "kg"));

            assertThatThrownBy(() -> sellerInventoryApi.createBatch(req, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(400));
        }
    }

    @Nested
    @DisplayName("VAL5 - Create instance for non-existent product returns 400")
    class IT_VAL5_InstanceNonExistentProduct {

        @Test
        @DisplayName("Should reject instance creation with non-existent productId")
        void shouldRejectInstanceForNonExistentProduct() throws Exception {
            CreateInventoryInstanceRequest req = new CreateInventoryInstanceRequest();
            req.setProductId(UUID.randomUUID());
            req.setQuantity(instanceQty("100", "kg"));

            assertThatThrownBy(() -> sellerInventoryApi.createInventoryInstance(req, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(400));
        }
    }

    @Nested
    @DisplayName("VAL6 - Get non-existent auction returns 404")
    class IT_VAL6_NonExistentAuction {

        @Test
        @DisplayName("Should return 404 for non-existent auction ID")
        void shouldReturn404ForNonExistentAuction() throws Exception {
            UUID randomId = UUID.randomUUID();

            assertThatThrownBy(() -> sellerAuctionsApi.getAuctionById(randomId, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(404));
        }
    }

    @Nested
    @DisplayName("VAL7 - Get non-existent batch returns 404")
    class IT_VAL7_NonExistentBatch {

        @Test
        @DisplayName("Should return 404 for non-existent batch ID")
        void shouldReturn404ForNonExistentBatch() throws Exception {
            UUID randomId = UUID.randomUUID();

            assertThatThrownBy(() -> sellerInventoryApi.getBatch(randomId, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(404));
        }
    }

    @Nested
    @DisplayName("VAL8 - Get non-existent inventory instance returns 404")
    class IT_VAL8_NonExistentInstance {

        @Test
        @DisplayName("Should return 404 for non-existent instance ID")
        void shouldReturn404ForNonExistentInstance() throws Exception {
            UUID randomId = UUID.randomUUID();

            assertThatThrownBy(() -> sellerInventoryApi.getInventoryInstance(randomId, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(404));
        }
    }

    @Nested
    @DisplayName("VAL9 - Get non-existent product returns 404")
    class IT_VAL9_NonExistentProduct {

        @Test
        @DisplayName("Should return 404 for non-existent product ID")
        void shouldReturn404ForNonExistentProduct() throws Exception {
            UUID randomId = UUID.randomUUID();

            assertThatThrownBy(() -> sellerProductsApi.getProductType(randomId, API_VERSION))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(404));
        }
    }

    @Nested
    @DisplayName("VAL10 - Wadium on non-existent auction returns success (no auction validation)")
    class IT_VAL10_WadiumNonExistentAuction {

        @Test
        @DisplayName("Should accept wadium deposit even for non-existent auction (local adapter)")
        void shouldAcceptWadiumOnNonExistentAuction() throws Exception {
            UUID randomAuctionId = UUID.randomUUID();

            DepositWadiumRequest req = new DepositWadiumRequest();
            req.setAmount(pln("500.00"));

            WadiumResponse response = buyerAuctionsApi.depositWadium(randomAuctionId, req, API_VERSION);
            assertThat(response.getStatus()).isEqualTo(WadiumResponse.StatusEnum.PAID);
        }
    }

    @Nested
    @DisplayName("VAL11 - Create instance with non-existent batchId succeeds (no batch validation)")
    class IT_VAL11_InstanceNonExistentBatch {

        @Test
        @DisplayName("Should create instance even with non-existent batchId (backend does not validate)")
        void shouldAcceptInstanceWithNonExistentBatch() throws Exception {
            CreateProductTypeRequest prodReq = productRequest("VAL11 batch product",
                    "Product for batch validation test", ProductTrackingStrategy.BATCH_TRACKED, "kg");
            UUID productId = sellerProductsApi.createProductType(prodReq, API_VERSION).getProductId();

            CreateInventoryInstanceRequest req = new CreateInventoryInstanceRequest();
            req.setProductId(productId);
            req.setBatchId(UUID.randomUUID());
            req.setQuantity(instanceQty("100", "kg"));

            InventoryInstanceResponse response = sellerInventoryApi.createInventoryInstance(req, API_VERSION);
            assertThat(response).isNotNull();
            assertThat(response.getInstanceId()).isNotNull();
        }
    }
}
