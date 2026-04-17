package com.github.hexabid.adapter.in.rest;

import com.github.hexabid.contract.api.ProductsApiDelegate;
import com.github.hexabid.contract.model.CreateProductTypeRequest;
import com.github.hexabid.contract.model.ProductTrackingStrategy;
import com.github.hexabid.contract.model.ProductTypeListResponse;
import com.github.hexabid.contract.model.ProductTypeResponse;
import com.github.hexabid.product.ProductDescription;
import com.github.hexabid.product.ProductIdentifier;
import com.github.hexabid.product.ProductName;
import com.github.hexabid.product.ProductType;
import com.github.hexabid.product.UuidProductIdentifier;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * REST delegate implementation for Products API.
 * 
 * ARCHETYP: Product (M02 - Katalog Produktów)
 * 
 * Produkt to definicja "co to jest" - niezależny kontekst biznesowy od Inventory.
 * ProductType reprezentuje szablon, według którego tworzone są instancje w inwentarzu.
 * 
 * Ścieżka danych:
 * 1. Administrator tworzy ProductType (co to jest produkt)
 * 2. Użytkownik tworzy Batch (partia produkcyjna)
 * 3. Użytkownik tworzy Instance (konkretna ilość z partii)
 * 4. Instance wystawiana na auction
 * 
 * @see <a href="doc/backend/products/README.md">Products Backend Documentation</a>
 */
@Service
public class RestProductApiDelegate implements ProductsApiDelegate {

    private final com.github.hexabid.product.ProductFacade productFacade;
    private final Counter browseProductTypesCounter;
    private final Counter createProductTypeCounter;
    private final Counter getProductTypeCounter;

    public RestProductApiDelegate(
            com.github.hexabid.product.ProductFacade productFacade,
            MeterRegistry meterRegistry
    ) {
        this.productFacade = productFacade;
        this.browseProductTypesCounter = meterRegistry.counter("products.browse.requests");
        this.createProductTypeCounter = meterRegistry.counter("products.create.requests");
        this.getProductTypeCounter = meterRegistry.counter("products.details.requests");
    }

    /**
     * GET /api/products - Browse all product types in catalog
     * 
     * Returns paginated list of all product types available in the system.
     * This is the main entry point for displaying the product catalog.
     * 
     * @param xApiVersion API version negotiated via HTTP header
     * @param query Filter by product name (partial match, case-insensitive)
     * @param trackingStrategy Filter by product tracking strategy
     * @param limit Maximum number of items to return (default 20)
     * @param after Cursor for pagination
     * @return Paginated list of product types
     */
    @Override
    public ResponseEntity<ProductTypeListResponse> browseProductTypes(
            String xApiVersion,
            String query,
            ProductTrackingStrategy trackingStrategy,
            Integer limit,
            String after
    ) {
        browseProductTypesCounter.increment();
        
        List<ProductType> allProducts = productFacade.allProductTypes();
        
        // Apply filters
        var filtered = allProducts.stream()
                .filter(p -> query == null || p.name().toString().toLowerCase().contains(query.toLowerCase()))
                .filter(p -> trackingStrategy == null || p.trackingStrategy() == toDomainTrackingStrategy(trackingStrategy))
                .toList();
        
        // Apply pagination
        int pageSize = limit != null ? Math.min(limit, 50) : 20;
        int startIndex = after != null ? parseCursor(after, allProducts.size()) : 0;
        int endIndex = Math.min(startIndex + pageSize, filtered.size());
        
        List<ProductTypeResponse> items = filtered.subList(startIndex, endIndex).stream()
                .map(this::toResponse)
                .toList();
        
        String nextCursor = endIndex < filtered.size() ? String.valueOf(endIndex) : null;
        
        ProductTypeListResponse response = new ProductTypeListResponse();
        response.setItems(items);
        response.setNextCursor(nextCursor);
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/products - Define a new product type
     * 
     * Creates a new product type in the catalog.
     * Typically done by administrators.
     * 
     * @param request Product type creation request
     * @param xApiVersion API version negotiated via HTTP header
     * @return Created product type
     */
    @Override
    public ResponseEntity<ProductTypeResponse> createProductType(
            CreateProductTypeRequest request,
            String xApiVersion
    ) {
        createProductTypeCounter.increment();
        
        var unit = new com.github.hexabid.quantity.Unit(request.getPreferredUnit(), request.getPreferredUnit());
        
        ProductType productType = createProductType(request.getName(), request.getDescription(), 
                request.getTrackingStrategy(), unit);
        
        ProductType created = productFacade.createProductType(productType);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(created));
    }

    /**
     * GET /api/products/{productId} - Get product type by ID
     * 
     * @param productId Product type UUID
     * @param xApiVersion API version negotiated via HTTP header
     * @return Product type details or 404
     */
    @Override
    public ResponseEntity<ProductTypeResponse> getProductType(UUID productId, String xApiVersion) {
        getProductTypeCounter.increment();
        
        ProductIdentifier id = ProductIdentifier.uuid(productId);
        return productFacade.findProductType(id)
                .map(p -> ResponseEntity.ok(toResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    private int parseCursor(String cursor, int maxSize) {
        try {
            int parsed = Integer.parseInt(cursor);
            return Math.max(0, Math.min(parsed, maxSize));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // === Mapping methods ===

    private com.github.hexabid.product.ProductTrackingStrategy toDomainTrackingStrategy(ProductTrackingStrategy strategy) {
        if (strategy == null) return com.github.hexabid.product.ProductTrackingStrategy.IDENTICAL;
        return switch (strategy) {
            case UNIQUE -> com.github.hexabid.product.ProductTrackingStrategy.UNIQUE;
            case INDIVIDUALLY_TRACKED -> com.github.hexabid.product.ProductTrackingStrategy.INDIVIDUALLY_TRACKED;
            case BATCH_TRACKED -> com.github.hexabid.product.ProductTrackingStrategy.BATCH_TRACKED;
            case INDIVIDUALLY_AND_BATCH_TRACKED -> com.github.hexabid.product.ProductTrackingStrategy.INDIVIDUALLY_AND_BATCH_TRACKED;
            case IDENTICAL -> com.github.hexabid.product.ProductTrackingStrategy.IDENTICAL;
        };
    }

    private ProductTypeResponse toResponse(ProductType productType) {
        UUID productId = null;
        if (productType.id() instanceof UuidProductIdentifier uuidId) {
            productId = uuidId.value();
        }
        
        ProductTypeResponse response = new ProductTypeResponse();
        response.setProductId(productId);
        response.setName(productType.name().toString());
        response.setDescription(productType.description() != null ? productType.description().toString() : null);
        response.setTrackingStrategy(toContractTrackingStrategy(productType.trackingStrategy()));
        response.setPreferredUnit(productType.preferredUnit().toString());
        return response;
    }

    private ProductTrackingStrategy toContractTrackingStrategy(com.github.hexabid.product.ProductTrackingStrategy strategy) {
        if (strategy == null) return ProductTrackingStrategy.IDENTICAL;
        return ProductTrackingStrategy.valueOf(strategy.name());
    }

    private ProductType createProductType(String name, String description, 
            ProductTrackingStrategy trackingStrategy, com.github.hexabid.quantity.Unit unit) {
        com.github.hexabid.product.ProductTrackingStrategy domainStrategy = toDomainTrackingStrategy(trackingStrategy);
        
        return switch (domainStrategy) {
            case UNIQUE -> ProductType.unique(ProductIdentifier.randomUuid(), 
                    new ProductName(name), new ProductDescription(description));
            case BATCH_TRACKED -> ProductType.batchTracked(ProductIdentifier.randomUuid(),
                    new ProductName(name), new ProductDescription(description), unit);
            case INDIVIDUALLY_TRACKED -> ProductType.individuallyTracked(ProductIdentifier.randomUuid(),
                    new ProductName(name), new ProductDescription(description), unit);
            case INDIVIDUALLY_AND_BATCH_TRACKED -> ProductType.individuallyAndBatchTracked(ProductIdentifier.randomUuid(),
                    new ProductName(name), new ProductDescription(description), unit);
            case IDENTICAL -> ProductType.identical(ProductIdentifier.randomUuid(),
                    new ProductName(name), new ProductDescription(description), unit);
        };
    }
}