package com.github.hexabid.integration.flow;

import com.github.hexabid.contract.client.ApiException;
import com.github.hexabid.contract.client.model.*;
import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Black-box integration tests for the <strong>Inventory Management</strong> domain (M06).
 *
 * <p>These tests exercise batch and inventory instance operations. A batch groups
 * products from the same production source; an inventory instance represents a specific
 * quantity of product that can be placed on auction.</p>
 *
 * <h3>Business flow</h3>
 * <pre>
 *   ProductType → Batch → InventoryInstance → (Lot → Auction)
 *       M02        M06         M06               M03
 * </pre>
 *
 * <h3>Covered endpoints</h3>
 * <ul>
 *   <li>{@code POST   /api/batches}                — createBatch</li>
 *   <li>{@code GET    /api/batches}                — browseBatches</li>
 *   <li>{@code GET    /api/batches/{id}}           — getBatch</li>
 *   <li>{@code POST   /api/inventory/instances}    — createInventoryInstance</li>
 *   <li>{@code GET    /api/inventory/instances}    — browseInventoryInstances</li>
 *   <li>{@code GET    /api/inventory/instances/{id}} — getInventoryInstance</li>
 * </ul>
 */
class InventoryManagementFlowIT extends IntegrationTestBase {

    /**
     * Creates a BATCH_TRACKED product and returns its ID.
     * Reused across multiple tests as a prerequisite.
     */
    private UUID createBatchTrackedProduct() throws Exception {
        CreateProductTypeRequest req = productRequest(
                "Inventory flow corn",
                "Kukurydza na ziarno, klasa A",
                ProductTrackingStrategy.BATCH_TRACKED,
                "kg"
        );
        return sellerProductsApi.createProductType(req, API_VERSION).getProductId();
    }

    /**
     * Creates a UNIQUE product and returns its ID.
     */
    private UUID createUniqueProduct() throws Exception {
        CreateProductTypeRequest req = productRequest(
                "Inventory flow car",
                "Audi A4 2016, diesel",
                ProductTrackingStrategy.UNIQUE,
                "pcs"
        );
        return sellerProductsApi.createProductType(req, API_VERSION).getProductId();
    }

    @Nested
    @DisplayName("F9 — Create a production batch for a BATCH_TRACKED product")
    class IT_F9_CreateBatch {

        @Test
        @DisplayName("Should create batch and return it with generated ID")
        void shouldCreateBatchForBatchTrackedProduct() throws Exception {
            UUID productId = createBatchTrackedProduct();

            CreateBatchRequest req = new CreateBatchRequest();
            req.setProductId(productId);
            req.setName(unique("KUKURYDZA-2026-POLAND-001"));
            req.setQuantity(batchQty("2000", "kg"));

            BatchResponse response = sellerInventoryApi.createBatch(req, API_VERSION);

            assertThat(response.getBatchId()).isNotNull();
            assertThat(response.getProductId()).isEqualTo(productId);
            assertThat(response.getName()).isEqualTo(req.getName());
            assertThat(response.getQuantity().getAmount()).isEqualTo("2000");
            assertThat(response.getQuantity().getUnit()).isEqualTo("kg");
        }
    }

    @Nested
    @DisplayName("F10 — Create batch with production and expiry dates")
    class IT_F10_CreateBatchWithDates {

        @Test
        @DisplayName("Should preserve dateProduced and bestBefore in the response")
        void shouldCreateBatchWithDates() throws Exception {
            UUID productId = createBatchTrackedProduct();

            OffsetDateTime produced = OffsetDateTime.now().minusMonths(2);
            OffsetDateTime bestBefore = OffsetDateTime.now().plusMonths(6);

            CreateBatchRequest req = new CreateBatchRequest();
            req.setProductId(productId);
            req.setName(unique("POTATO-2025-WARSZAWA-001"));
            req.setQuantity(batchQty("5000", "kg"));
            req.setDateProduced(produced);
            req.setBestBefore(bestBefore);

            BatchResponse response = sellerInventoryApi.createBatch(req, API_VERSION);

            assertThat(response.getDateProduced()).isNotNull();
            assertThat(response.getBestBefore()).isNotNull();
        }
    }

    @Nested
    @DisplayName("F11 — Get batch by ID")
    class IT_F11_GetBatchById {

        @Test
        @DisplayName("Should retrieve batch details by batchId")
        void shouldGetBatchById() throws Exception {
            UUID productId = createBatchTrackedProduct();

            CreateBatchRequest req = new CreateBatchRequest();
            req.setProductId(productId);
            req.setName(unique("GET-BATCH-TEST-001"));
            req.setQuantity(batchQty("1000", "kg"));
            BatchResponse created = sellerInventoryApi.createBatch(req, API_VERSION);

            BatchResponse fetched = sellerInventoryApi.getBatch(created.getBatchId(), API_VERSION);

            assertThat(fetched.getBatchId()).isEqualTo(created.getBatchId());
            assertThat(fetched.getProductId()).isEqualTo(productId);
            assertThat(fetched.getName()).isEqualTo(created.getName());
        }
    }

    @Nested
    @DisplayName("F12 — Browse batches filtered by product")
    class IT_F12_BrowseBatchesByProduct {

        @Test
        @DisplayName("Should return only batches for the specified product")
        void shouldFilterBatchesByProduct() throws Exception {
            UUID productId = createBatchTrackedProduct();

            CreateBatchRequest req = new CreateBatchRequest();
            req.setProductId(productId);
            req.setName(unique("FILTER-BATCH-TEST"));
            req.setQuantity(batchQty("500", "kg"));
            sellerInventoryApi.createBatch(req, API_VERSION);

            BatchListResponse response = sellerInventoryApi.browseBatches(
                    API_VERSION, productId, null, null, null);

            assertThat(response.getItems()).isNotEmpty();
            assertThat(response.getItems())
                    .allSatisfy(b -> assertThat(b.getProductId()).isEqualTo(productId));
        }
    }

    @Nested
    @DisplayName("F13 — Create inventory instance from batch")
    class IT_F13_CreateInstanceFromBatch {

        @Test
        @DisplayName("Should create instance linked to product and batch")
        void shouldCreateInstanceFromBatch() throws Exception {
            UUID productId = createBatchTrackedProduct();

            CreateBatchRequest batchReq = new CreateBatchRequest();
            batchReq.setProductId(productId);
            batchReq.setName(unique("INSTANCE-BATCH-001"));
            batchReq.setQuantity(batchQty("2000", "kg"));
            UUID batchId = sellerInventoryApi.createBatch(batchReq, API_VERSION).getBatchId();

            CreateInventoryInstanceRequest instanceReq = new CreateInventoryInstanceRequest();
            instanceReq.setProductId(productId);
            instanceReq.setBatchId(batchId);
            instanceReq.setQuantity(instanceQty("100", "kg"));

            InventoryInstanceResponse response = sellerInventoryApi.createInventoryInstance(instanceReq, API_VERSION);

            assertThat(response.getInstanceId()).isNotNull();
            assertThat(response.getProductId()).isEqualTo(productId);
            assertThat(response.getBatchId()).isEqualTo(batchId);
            assertThat(response.getQuantity().getAmount()).isEqualTo("100");
            assertThat(response.getQuantity().getUnit()).isEqualTo("kg");
        }
    }

    @Nested
    @DisplayName("F14 — Create inventory instance with serial number (UNIQUE product)")
    class IT_F14_CreateInstanceWithSerialNumber {

        @Test
        @DisplayName("Should create instance with serial number for a unique product")
        void shouldCreateInstanceWithSerialNumber() throws Exception {
            UUID productId = createUniqueProduct();

            CreateInventoryInstanceRequest req = new CreateInventoryInstanceRequest();
            req.setProductId(productId);
            req.setSerialNumber("WVWZZZ3CZWE123456");
            req.setQuantity(instanceQty("1", "pcs"));

            InventoryInstanceResponse response = sellerInventoryApi.createInventoryInstance(req, API_VERSION);

            assertThat(response.getInstanceId()).isNotNull();
            assertThat(response.getSerialNumber()).isEqualTo("WVWZZZ3CZWE123456");
            assertThat(response.getProductId()).isEqualTo(productId);
        }
    }

    @Nested
    @DisplayName("F15 — Get inventory instance by ID")
    class IT_F15_GetInstanceById {

        @Test
        @DisplayName("Should retrieve instance details by instanceId")
        void shouldGetInstanceById() throws Exception {
            UUID productId = createUniqueProduct();

            CreateInventoryInstanceRequest req = new CreateInventoryInstanceRequest();
            req.setProductId(productId);
            req.setSerialNumber("VIN-GET-BY-ID-TEST");
            req.setQuantity(instanceQty("1", "pcs"));
            InventoryInstanceResponse created = sellerInventoryApi.createInventoryInstance(req, API_VERSION);

            InventoryInstanceResponse fetched = sellerInventoryApi.getInventoryInstance(created.getInstanceId(), API_VERSION);

            assertThat(fetched.getInstanceId()).isEqualTo(created.getInstanceId());
            assertThat(fetched.getProductId()).isEqualTo(productId);
            assertThat(fetched.getSerialNumber()).isEqualTo("VIN-GET-BY-ID-TEST");
        }
    }

    @Nested
    @DisplayName("F16 — Browse inventory instances filtered by product")
    class IT_F16_BrowseInstancesByProduct {

        @Test
        @DisplayName("Should return only instances for the specified product")
        void shouldFilterInstancesByProduct() throws Exception {
            UUID productId = createBatchTrackedProduct();

            CreateInventoryInstanceRequest req = new CreateInventoryInstanceRequest();
            req.setProductId(productId);
            req.setQuantity(instanceQty("50", "kg"));
            sellerInventoryApi.createInventoryInstance(req, API_VERSION);

            InventoryInstanceListResponse response = sellerInventoryApi.browseInventoryInstances(
                    API_VERSION, null, productId, null, null);

            assertThat(response.getItems()).isNotEmpty();
            assertThat(response.getItems())
                    .allSatisfy(i -> assertThat(i.getProductId()).isEqualTo(productId));
        }
    }

    @Nested
    @DisplayName("F17 — Negative: Create batch for non-existent product")
    class IT_F17_NegativeBatchCreation {

        @Test
        @DisplayName("Should reject batch creation when productId does not exist")
        void shouldRejectBatchForNonExistentProduct() {
            CreateBatchRequest req = new CreateBatchRequest();
            req.setProductId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            req.setName(unique("PHANTOM-BATCH"));
            req.setQuantity(batchQty("100", "kg"));

            assertThatExceptionOfType(ApiException.class)
                    .isThrownBy(() -> sellerInventoryApi.createBatch(req, API_VERSION));
        }
    }
}
