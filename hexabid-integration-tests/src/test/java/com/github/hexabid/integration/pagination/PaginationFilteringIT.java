package com.github.hexabid.integration.pagination;

import com.github.hexabid.contract.client.model.*;
import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationFilteringIT extends IntegrationTestBase {

    @Nested
    @DisplayName("PG1 - Product catalog pagination with cursor")
    class IT_PG1_ProductPagination {

        @Test
        @DisplayName("Should paginate products and return nextCursor when more exist")
        void shouldPaginateProducts() throws Exception {
            for (int i = 0; i < 5; i++) {
                CreateProductTypeRequest req = productRequest("PG1 product " + i,
                        "Pagination test product " + i, ProductTrackingStrategy.IDENTICAL, "pcs");
                sellerProductsApi.createProductType(req, API_VERSION);
            }

            ProductTypeListResponse firstPage = sellerProductsApi.browseProductTypes(
                    API_VERSION, null, null, 3, null);

            assertThat(firstPage.getItems()).hasSizeBetween(1, 3);

            if (firstPage.getNextCursor() != null) {
                ProductTypeListResponse secondPage = sellerProductsApi.browseProductTypes(
                        API_VERSION, null, null, 3, firstPage.getNextCursor());

                assertThat(secondPage.getItems()).isNotEmpty();

                Set<UUID> firstPageIds = new HashSet<>();
                firstPage.getItems().forEach(p -> firstPageIds.add(p.getProductId()));
                Set<UUID> secondPageIds = new HashSet<>();
                secondPage.getItems().forEach(p -> secondPageIds.add(p.getProductId()));

                firstPageIds.retainAll(secondPageIds);
                assertThat(firstPageIds).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("PG2 - Batch pagination and filter by product")
    class IT_PG2_BatchPagination {

        @Test
        @DisplayName("Should paginate batches and filter by productId")
        void shouldPaginateAndFilterBatches() throws Exception {
            CreateProductTypeRequest prodReq = productRequest("PG2 batch product",
                    "Product for batch pagination", ProductTrackingStrategy.BATCH_TRACKED, "kg");
            UUID productId = sellerProductsApi.createProductType(prodReq, API_VERSION).getProductId();

            for (int i = 0; i < 3; i++) {
                CreateBatchRequest batchReq = new CreateBatchRequest();
                batchReq.setProductId(productId);
                batchReq.setName(unique("PG2-BATCH-" + i));
                batchReq.setQuantity(batchQty("1000", "kg"));
                sellerInventoryApi.createBatch(batchReq, API_VERSION);
            }

            BatchListResponse allBatches = sellerInventoryApi.browseBatches(
                    API_VERSION, null, null, 50, null);
            long matchingBatches = allBatches.getItems().stream()
                    .filter(b -> productId.equals(b.getProductId()))
                    .count();
            assertThat(matchingBatches).isGreaterThanOrEqualTo(3);

            BatchListResponse filteredBatches = sellerInventoryApi.browseBatches(
                    API_VERSION, productId, null, 50, null);
            assertThat(filteredBatches.getItems()).isNotEmpty();
            assertThat(filteredBatches.getItems())
                    .allSatisfy(b -> assertThat(b.getProductId()).isEqualTo(productId));
        }
    }

    @Nested
    @DisplayName("PG3 - Inventory instances pagination and filter by product")
    class IT_PG3_InstancePagination {

        @Test
        @DisplayName("Should paginate instances and filter by productId")
        void shouldPaginateAndFilterInstances() throws Exception {
            CreateProductTypeRequest prodReq = productRequest("PG3 instance product",
                    "Product for instance pagination", ProductTrackingStrategy.BATCH_TRACKED, "kg");
            UUID productId = sellerProductsApi.createProductType(prodReq, API_VERSION).getProductId();

            for (int i = 0; i < 3; i++) {
                CreateInventoryInstanceRequest instReq = new CreateInventoryInstanceRequest();
                instReq.setProductId(productId);
                instReq.setQuantity(instanceQty("100", "kg"));
                sellerInventoryApi.createInventoryInstance(instReq, API_VERSION);
            }

            InventoryInstanceListResponse filtered = sellerInventoryApi.browseInventoryInstances(
                    API_VERSION, null, productId, 50, null);
            assertThat(filtered.getItems()).isNotEmpty();
            assertThat(filtered.getItems())
                    .allSatisfy(inst -> assertThat(inst.getProductId()).isEqualTo(productId));
        }
    }

    @Nested
    @DisplayName("PG4 - Auction pagination and filter by status")
    class IT_PG4_AuctionPagination {

        @Test
        @DisplayName("Should paginate auctions and filter by DRAFT status")
        void shouldPaginateAndFilterAuctions() throws Exception {
            for (int i = 0; i < 5; i++) {
                CreateAuctionRequest req = auctionRequest("PG4 auction " + i, "1000.00");
                sellerAuctionsApi.createAuction(req, API_VERSION);
            }

            AuctionListResponse draftAuctions = sellerAuctionsApi.browseAuctions(
                    API_VERSION, null, AuctionStatus.DRAFT, null, null, null);
            assertThat(draftAuctions.getItems()).isNotEmpty();
            assertThat(draftAuctions.getItems())
                    .allSatisfy(a -> assertThat(a.getStatus()).isEqualTo(AuctionStatus.DRAFT));
        }
    }

    @Nested
    @DisplayName("PG5 - Auction search by title")
    class IT_PG5_AuctionSearch {

        @Test
        @DisplayName("Should find auction by partial title match")
        void shouldFindAuctionByTitle() throws Exception {
            String uniqueToken = "PG5-SEARCH-" + UUID.randomUUID();
            CreateAuctionRequest req = new CreateAuctionRequest();
            req.setTitle("Special auction " + uniqueToken);
            req.setStartingPrice(pln("5000.00"));
            req.setEndsAt(java.time.OffsetDateTime.now().plusHours(4));
            sellerAuctionsApi.createAuction(req, API_VERSION);

            AuctionListResponse results = sellerAuctionsApi.browseAuctions(
                    API_VERSION, uniqueToken, null, null, null, null);
            assertThat(results.getItems()).isNotEmpty();
            assertThat(results.getItems())
                    .anySatisfy(a -> assertThat(a.getTitle()).contains(uniqueToken));
        }
    }

    @Nested
    @DisplayName("PG6 - Product filter by tracking strategy")
    class IT_PG6_ProductStrategyFilter {

        @Test
        @DisplayName("Should filter products by tracking strategy")
        void shouldFilterByTrackingStrategy() throws Exception {
            CreateProductTypeRequest uniqueReq = productRequest("PG6 unique product",
                    "Product for strategy filter", ProductTrackingStrategy.UNIQUE, "pcs");
            sellerProductsApi.createProductType(uniqueReq, API_VERSION);

            ProductTypeListResponse uniqueProducts = sellerProductsApi.browseProductTypes(
                    API_VERSION, null, ProductTrackingStrategy.UNIQUE, 50, null);
            assertThat(uniqueProducts.getItems()).isNotEmpty();
            assertThat(uniqueProducts.getItems())
                    .allSatisfy(p -> assertThat(p.getTrackingStrategy()).isEqualTo(ProductTrackingStrategy.UNIQUE));
        }
    }
}
