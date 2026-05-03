package com.github.hexabid.integration.flow;

import com.github.hexabid.contract.client.model.*;
import com.github.hexabid.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

/**
 * Black-box integration tests for the <strong>Product Catalog</strong> domain (M02).
 *
 * <p>These tests exercise the product type lifecycle via the generated OpenAPI client,
 * treating the running service as a black box — we only know the contract, not the
 * implementation. Each nested class documents a distinct business scenario.</p>
 *
 * <h3>Business context</h3>
 * <p>A ProductType is a <em>template</em> — it defines "what something is" and how it
 * should be tracked through the supply chain. Before any inventory or auction can exist,
 * the product type must be registered in the catalog. The tracking strategy determines
 * how instances of this product are identified downstream (by serial number, by batch,
 * or as identical interchangeable units).</p>
 *
 * <h3>Covered endpoints</h3>
 * <ul>
 *   <li>{@code POST   /api/products}           — createProductType</li>
 *   <li>{@code GET    /api/products}           — browseProductTypes</li>
 *   <li>{@code GET    /api/products/{id}}      — getProductType</li>
 * </ul>
 */
class ProductCatalogFlowIT extends IntegrationTestBase {

    @Nested
    @DisplayName("F1 — Create a UNIQUE product type")
    class IT_F1_CreateUniqueProduct {

        @Test
        @DisplayName("Should create a car product tracked by individual serial number")
        void shouldCreateUniqueProductType() throws Exception {
            CreateProductTypeRequest req = productRequest(
                    "Seat Leon 1999 TDI",
                    "Silver sedan, VIN: WVWZZZ3CZWE123456",
                    ProductTrackingStrategy.UNIQUE,
                    "pcs"
            );

            ProductTypeResponse response = sellerProductsApi.createProductType(req, API_VERSION);

            assertThat(response.getProductId()).isNotNull();
            assertThat(response.getName()).isEqualTo(req.getName());
            assertThat(response.getDescription()).isEqualTo(req.getDescription());
            assertThat(response.getTrackingStrategy()).isEqualTo(ProductTrackingStrategy.UNIQUE);
            assertThat(response.getPreferredUnit()).isEqualTo("pcs");
        }
    }

    @Nested
    @DisplayName("F2 — Create a BATCH_TRACKED product type")
    class IT_F2_CreateBatchTrackedProduct {

        @Test
        @DisplayName("Should create an agricultural commodity tracked by production batch")
        void shouldCreateBatchTrackedProduct() throws Exception {
            CreateProductTypeRequest req = productRequest(
                    "Kukurydza paszowa",
                    "Kukurydza na ziarno, klasa A",
                    ProductTrackingStrategy.BATCH_TRACKED,
                    "kg"
            );

            ProductTypeResponse response = sellerProductsApi.createProductType(req, API_VERSION);

            assertThat(response.getProductId()).isNotNull();
            assertThat(response.getTrackingStrategy()).isEqualTo(ProductTrackingStrategy.BATCH_TRACKED);
            assertThat(response.getPreferredUnit()).isEqualTo("kg");
        }
    }

    @Nested
    @DisplayName("F3 — Create an INDIVIDUALLY_TRACKED product type")
    class IT_F3_CreateIndividuallyTrackedProduct {

        @Test
        @DisplayName("Should create electronics tracked by serial number")
        void shouldCreateIndividuallyTrackedProduct() throws Exception {
            CreateProductTypeRequest req = productRequest(
                    "MacBook Pro 16\"",
                    "Apple laptop, M3 Pro, 36 GB RAM",
                    ProductTrackingStrategy.INDIVIDUALLY_TRACKED,
                    "pcs"
            );

            ProductTypeResponse response = sellerProductsApi.createProductType(req, API_VERSION);

            assertThat(response.getProductId()).isNotNull();
            assertThat(response.getTrackingStrategy()).isEqualTo(ProductTrackingStrategy.INDIVIDUALLY_TRACKED);
        }
    }

    @Nested
    @DisplayName("F4 — Create an IDENTICAL product type")
    class IT_F4_CreateIdenticalProduct {

        @Test
        @DisplayName("Should create interchangeable fasteners with no individual tracking")
        void shouldCreateIdenticalProduct() throws Exception {
            CreateProductTypeRequest req = productRequest(
                    "Śruba M10x40 kl. 8.8",
                    "Śruba sześciokątna, ocynk",
                    ProductTrackingStrategy.IDENTICAL,
                    "pcs"
            );

            ProductTypeResponse response = sellerProductsApi.createProductType(req, API_VERSION);

            assertThat(response.getProductId()).isNotNull();
            assertThat(response.getTrackingStrategy()).isEqualTo(ProductTrackingStrategy.IDENTICAL);
        }
    }

    @Nested
    @DisplayName("F5 — Browse product catalog")
    class IT_F5_BrowseProductCatalog {

        @Test
        @DisplayName("Should return paginated list of all product types")
        void shouldBrowseAllProductTypes() throws Exception {
            CreateProductTypeRequest req = productRequest(
                    "Browse test product",
                    "Product for catalog browsing test",
                    ProductTrackingStrategy.BATCH_TRACKED,
                    "t"
            );
            sellerProductsApi.createProductType(req, API_VERSION);

            ProductTypeListResponse response = sellerProductsApi.browseProductTypes(
                    API_VERSION, null, null, null, null);

            assertThat(response.getItems()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("F6 — Get product type by ID")
    class IT_F6_GetProductTypeById {

        @Test
        @DisplayName("Should retrieve the exact product type created earlier")
        void shouldRetrieveProductTypeById() throws Exception {
            CreateProductTypeRequest req = productRequest(
                    "Lookup test product",
                    "Product for get-by-ID test",
                    ProductTrackingStrategy.UNIQUE,
                    "pcs"
            );
            ProductTypeResponse created = sellerProductsApi.createProductType(req, API_VERSION);

            ProductTypeResponse fetched = sellerProductsApi.getProductType(created.getProductId(), API_VERSION);

            assertThat(fetched.getProductId()).isEqualTo(created.getProductId());
            assertThat(fetched.getName()).isEqualTo(created.getName());
            assertThat(fetched.getTrackingStrategy()).isEqualTo(created.getTrackingStrategy());
            assertThat(fetched.getPreferredUnit()).isEqualTo(created.getPreferredUnit());
        }
    }

    @Nested
    @DisplayName("F7 — Filter products by tracking strategy")
    class IT_F7_FilterByTrackingStrategy {

        @Test
        @DisplayName("Should return only UNIQUE products when filtered")
        void shouldFilterByTrackingStrategy() throws Exception {
            CreateProductTypeRequest uniqueReq = productRequest(
                    "Strategy filter unique",
                    "UNIQUE product for strategy filter test",
                    ProductTrackingStrategy.UNIQUE,
                    "pcs"
            );
            sellerProductsApi.createProductType(uniqueReq, API_VERSION);

            ProductTypeListResponse response = sellerProductsApi.browseProductTypes(
                    API_VERSION, null, ProductTrackingStrategy.UNIQUE, null, null);

            assertThat(response.getItems()).isNotEmpty();
            assertThat(response.getItems())
                    .allSatisfy(item ->
                            assertThat(item.getTrackingStrategy()).isEqualTo(ProductTrackingStrategy.UNIQUE));
        }
    }

    @Nested
    @DisplayName("F8 — Search products by name")
    class IT_F8_SearchProductsByName {

        @Test
        @DisplayName("Should find products matching a partial name query")
        void shouldSearchProductsByName() throws Exception {
            String distinctiveName = "ZYXWVUTSRQP_" + UUID.randomUUID();
            CreateProductTypeRequest req = productRequest(
                    distinctiveName,
                    "Product for name search test",
                    ProductTrackingStrategy.IDENTICAL,
                    "pcs"
            );
            sellerProductsApi.createProductType(req, API_VERSION);

            ProductTypeListResponse response = sellerProductsApi.browseProductTypes(
                    API_VERSION, "ZYXWVUTSRQP", null, null, null);

            assertThat(response.getItems()).isNotEmpty();
            assertThat(response.getItems())
                    .anySatisfy(item -> assertThat(item.getName()).contains(distinctiveName));
        }
    }
}
