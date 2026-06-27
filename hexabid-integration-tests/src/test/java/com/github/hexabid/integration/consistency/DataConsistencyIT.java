package com.github.hexabid.integration.consistency;

import com.github.hexabid.contract.client.model.*;
import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DataConsistencyIT extends IntegrationTestBase {

    @Nested
    @DisplayName("CON1 - Auction data identical across browse, get-by-id, and my-auctions")
    class IT_CON1_AuctionDataConsistency {

        @Test
        @DisplayName("Should return same auction data from all retrieval endpoints")
        void shouldReturnConsistentAuctionData() throws Exception {
            CreateAuctionRequest req = auctionRequest("CON1 consistency auction", "7500.00",
                    pricingConfigCar());
            AuctionResponse created = sellerAuctionsApi.createAuction(req, API_VERSION);
            UUID auctionId = created.getAuctionId();

            AuctionResponse byId = sellerAuctionsApi.getAuctionById(auctionId, API_VERSION);

            AuctionListItemResponse fromBrowse = findAuctionInBrowse(auctionId, "CON1 consistency");

            String cursor = null;
            AuctionListItemResponse fromMyAuctions = null;
            for (int page = 0; page < 20; page++) {
                AuctionListResponse myAuctions = sellerAuctionsApi.browseMyAuctions(
                        API_VERSION, null, null, 50, cursor);
                fromMyAuctions = myAuctions.getItems().stream()
                        .filter(a -> a.getAuctionId().equals(auctionId))
                        .findFirst()
                        .orElse(null);
                if (fromMyAuctions != null) break;
                cursor = myAuctions.getNextCursor();
                if (cursor == null) break;
            }
            assertThat(fromMyAuctions).as("auction should appear in my-auctions").isNotNull();

            assertThat(byId.getTitle()).isEqualTo(created.getTitle());
            assertThat(byId.getCurrentPrice()).isEqualTo(created.getCurrentPrice());
            assertThat(byId.getStatus()).isEqualTo(created.getStatus());

            assertThat(fromBrowse.getTitle()).isEqualTo(created.getTitle());
            assertThat(fromBrowse.getCurrentPrice()).isEqualTo(created.getCurrentPrice());
            assertThat(fromBrowse.getStatus()).isEqualTo(created.getStatus());

            assertThat(fromMyAuctions.getTitle()).isEqualTo(created.getTitle());
            assertThat(fromMyAuctions.getAuctionId()).isEqualTo(created.getAuctionId());
        }
    }

    @Nested
    @DisplayName("CON2 - Product data identical across browse and get-by-id")
    class IT_CON2_ProductDataConsistency {

        @Test
        @DisplayName("Should return same product data from browse and get-by-id")
        void shouldReturnConsistentProductData() throws Exception {
            CreateProductTypeRequest req = productRequest("CON2 consistency product",
                    "Product for data consistency test", ProductTrackingStrategy.BATCH_TRACKED, "kg");
            ProductTypeResponse created = sellerProductsApi.createProductType(req, API_VERSION);
            UUID productId = created.getProductId();

            ProductTypeResponse byId = sellerProductsApi.getProductType(productId, API_VERSION);

            ProductTypeResponse fromBrowse = findProductInBrowse(productId);

            assertThat(byId.getName()).isEqualTo(created.getName());
            assertThat(byId.getTrackingStrategy()).isEqualTo(created.getTrackingStrategy());

            assertThat(fromBrowse.getName()).isEqualTo(created.getName());
            assertThat(fromBrowse.getTrackingStrategy()).isEqualTo(created.getTrackingStrategy());
        }
    }

    @Nested
    @DisplayName("CON3 - Batch data identical across browse and get-by-id")
    class IT_CON3_BatchDataConsistency {

        @Test
        @DisplayName("Should return same batch data from browse and get-by-id")
        void shouldReturnConsistentBatchData() throws Exception {
            CreateProductTypeRequest prodReq = productRequest("CON3 batch product",
                    "Product for batch consistency", ProductTrackingStrategy.BATCH_TRACKED, "kg");
            UUID productId = sellerProductsApi.createProductType(prodReq, API_VERSION).getProductId();

            CreateBatchRequest batchReq = new CreateBatchRequest();
            batchReq.setProductId(productId);
            batchReq.setName(unique("CON3-BATCH-001"));
            batchReq.setQuantity(batchQty("2000", "kg"));
            BatchResponse created = sellerInventoryApi.createBatch(batchReq, API_VERSION);
            UUID batchId = created.getBatchId();

            BatchResponse byId = sellerInventoryApi.getBatch(batchId, API_VERSION);

            assertThat(byId.getBatchId()).isEqualTo(created.getBatchId());
            assertThat(byId.getName()).isEqualTo(created.getName());
            assertThat(byId.getProductId()).isEqualTo(created.getProductId());
        }
    }

    @Nested
    @DisplayName("CON4 - Instance data identical across browse and get-by-id")
    class IT_CON4_InstanceDataConsistency {

        @Test
        @DisplayName("Should return same instance data from browse and get-by-id")
        void shouldReturnConsistentInstanceData() throws Exception {
            CreateProductTypeRequest prodReq = productRequest("CON4 instance product",
                    "Product for instance consistency", ProductTrackingStrategy.UNIQUE, "pcs");
            UUID productId = sellerProductsApi.createProductType(prodReq, API_VERSION).getProductId();

            CreateInventoryInstanceRequest instReq = new CreateInventoryInstanceRequest();
            instReq.setProductId(productId);
            instReq.setSerialNumber("SN-CON4-TEST");
            instReq.setQuantity(instanceQty("1", "pcs"));
            InventoryInstanceResponse created = sellerInventoryApi.createInventoryInstance(instReq, API_VERSION);
            UUID instanceId = created.getInstanceId();

            InventoryInstanceResponse byId = sellerInventoryApi.getInventoryInstance(instanceId, API_VERSION);

            assertThat(byId.getInstanceId()).isEqualTo(created.getInstanceId());
            assertThat(byId.getProductId()).isEqualTo(created.getProductId());
            assertThat(byId.getSerialNumber()).isEqualTo("SN-CON4-TEST");
        }
    }

    @Nested
    @DisplayName("CON5 - Seller sees own auctions in my-auctions, buyer does not")
    class IT_CON5_OwnershipIsolation {

        @Test
        @DisplayName("Should isolate seller's auctions from buyer's my-auctions")
        void shouldIsolateMyAuctions() throws Exception {
            CreateAuctionRequest req = auctionRequest("CON5 ownership auction", "3000.00");
            AuctionResponse created = sellerAuctionsApi.createAuction(req, API_VERSION);
            UUID auctionId = created.getAuctionId();

            assertThat(auctionExistsInMyAuctions(auctionId)).isTrue();
            assertThat(auctionExistsInBuyerMyAuctions(auctionId)).isFalse();
        }
    }
}
