package com.acme.auctions.adapter.in.rest;

import com.acme.auctions.contract.api.InventoryApiDelegate;
import com.acme.auctions.contract.model.*;
import com.acme.auctions.product.*;
import com.acme.auctions.product.BatchId;
import com.acme.auctions.inventory.*;
import com.acme.auctions.quantity.Quantity;
import com.acme.auctions.quantity.Unit;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST delegate implementation for Inventory API.
 * 
 * ARCHETYP: Inventory (M06 - Inwentarz)
 * 
 * Inventory to kontekst "co mamy fizycznie" - w przeciwieństwie do Product (definicja).
 * Ścieżka danych:
 * 1. ProductType (definicja) → ProductFacade
 * 2. Batch (partia produkcyjna) → ProductFacade  
 * 3. InventoryEntry + Instance → InventoryFacade
 * 4. Lot (wystawienie na sprzedaż) → auctions-core
 * 
 * @see <a href="doc/backend/inventory/README.md">Inventory Backend Documentation</a>
 */
@Service
public class RestInventoryApiDelegate implements InventoryApiDelegate {

    private final com.acme.auctions.product.ProductFacade productFacade;
    private final InventoryFacade inventoryFacade;
    private final Counter browseBatchesCounter;
    private final Counter createBatchCounter;
    private final Counter getBatchCounter;
    private final Counter browseInstancesCounter;
    private final Counter createInstanceCounter;
    private final Counter getInstanceCounter;

    public RestInventoryApiDelegate(
            com.acme.auctions.product.ProductFacade productFacade,
            InventoryFacade inventoryFacade,
            MeterRegistry meterRegistry
    ) {
        this.productFacade = productFacade;
        this.inventoryFacade = inventoryFacade;
        this.browseBatchesCounter = meterRegistry.counter("inventory.browse.batches.requests");
        this.createBatchCounter = meterRegistry.counter("inventory.create.batch.requests");
        this.getBatchCounter = meterRegistry.counter("inventory.get.batch.requests");
        this.browseInstancesCounter = meterRegistry.counter("inventory.browse.instances.requests");
        this.createInstanceCounter = meterRegistry.counter("inventory.create.instance.requests");
        this.getInstanceCounter = meterRegistry.counter("inventory.get.instance.requests");
    }

    /**
     * GET /api/batches - Browse batches for current user
     */
    @Override
    public ResponseEntity<BatchListResponse> browseBatches(
            String xApiVersion,
            UUID productId,
            String query,
            Integer limit,
            String after
    ) {
        browseBatchesCounter.increment();
        
        List<Batch> allBatches = productFacade.allBatches();
        
        // Apply filters
        var filtered = allBatches.stream()
                .filter(b -> productId == null || (b.batchOf() instanceof UuidProductIdentifier uuidId && 
                        uuidId.value().equals(productId)))
                .filter(b -> query == null || b.name().toString().toLowerCase().contains(query.toLowerCase()))
                .toList();
        
        // Apply pagination
        int pageSize = limit != null ? Math.min(limit, 50) : 20;
        int startIndex = after != null ? parseCursor(after, allBatches.size()) : 0;
        int endIndex = Math.min(startIndex + pageSize, filtered.size());
        
        List<BatchResponse> items = filtered.subList(startIndex, endIndex).stream()
                .map(this::toBatchResponse)
                .toList();
        
        String nextCursor = endIndex < filtered.size() ? String.valueOf(endIndex) : null;
        
        BatchListResponse response = new BatchListResponse();
        response.setItems(items);
        response.setNextCursor(nextCursor);
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/batches - Create a new batch
     */
    @Override
    public ResponseEntity<BatchResponse> createBatch(CreateBatchRequest request, String xApiVersion) {
        createBatchCounter.increment();
        
        // Validate product exists
        UUID productIdUuid = request.getProductId();
        ProductIdentifier productId = ProductIdentifier.uuid(productIdUuid);
        if (productFacade.findProductType(productId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        // Map quantity
        Quantity quantity = new Quantity(
                new BigDecimal(request.getQuantity().getAmount()),
                new Unit(request.getQuantity().getUnit(), request.getQuantity().getUnit())
        );
        
        // Build batch
        Batch batch = Batch.builder()
                .id(BatchId.random())
                .name(new BatchName(request.getName()))
                .batchOf(productId)
                .quantityInBatch(quantity)
                .dateProduced(request.getDateProduced() != null ? request.getDateProduced().toInstant() : null)
                .bestBefore(request.getBestBefore() != null ? request.getBestBefore().toInstant() : null)
                .build();
        
        Batch created = productFacade.createBatch(batch);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toBatchResponse(created));
    }

    /**
     * GET /api/batches/{batchId} - Get batch by ID
     */
    @Override
    public ResponseEntity<BatchResponse> getBatch(UUID batchId, String xApiVersion) {
        getBatchCounter.increment();
        
        BatchId id = new BatchId(batchId);
        return productFacade.findBatch(id)
                .map(b -> ResponseEntity.ok(toBatchResponse(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/inventory/instances - Browse inventory instances
     */
    @Override
    public ResponseEntity<InventoryInstanceListResponse> browseInventoryInstances(
            String xApiVersion,
            UUID batchId,
            UUID productId,
            Integer limit,
            String after
    ) {
        browseInstancesCounter.increment();
        
        // Get all instances from inventory
        List<InventoryEntry> entries = inventoryFacade.allEntries();
        
        var allInstances = entries.stream()
                .flatMap(e -> e.instances().stream())
                .toList();
        
        // Apply filters
        var filtered = allInstances.stream()
                .filter(i -> batchId == null || (i.maybeBatchId().isPresent() && 
                        i.maybeBatchId().get().value().equals(batchId)))
                .filter(i -> productId == null || (i.productId() instanceof UuidProductIdentifier uuidId && 
                        uuidId.value().equals(productId)))
                .toList();
        
        // Apply pagination
        int pageSize = limit != null ? Math.min(limit, 50) : 20;
        int startIndex = after != null ? parseCursor(after, allInstances.size()) : 0;
        int endIndex = Math.min(startIndex + pageSize, filtered.size());
        
        List<InventoryInstanceResponse> items = filtered.subList(startIndex, endIndex).stream()
                .map(this::toInstanceResponse)
                .toList();
        
        String nextCursor = endIndex < filtered.size() ? String.valueOf(endIndex) : null;
        
        InventoryInstanceListResponse response = new InventoryInstanceListResponse();
        response.setItems(items);
        response.setNextCursor(nextCursor);
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/inventory/instances - Create inventory instance
     */
    @Override
    public ResponseEntity<InventoryInstanceResponse> createInventoryInstance(
            CreateInventoryInstanceRequest request,
            String xApiVersion
    ) {
        createInstanceCounter.increment();
        
        // Find or create inventory entry for product
        UUID productIdUuid = request.getProductId();
        ProductIdentifier productId = ProductIdentifier.uuid(productIdUuid);
        
        // Get inventory entry - for now create if not exists
        List<InventoryEntry> entries = inventoryFacade.entriesForProduct(productId);
        InventoryEntry entry;
        if (entries.isEmpty()) {
            entry = inventoryFacade.createInventoryEntry(new InventoryProduct(productId));
        } else {
            entry = entries.get(0);
        }
        
        // Create instance based on whether batchId is provided
        Instance instance;
        UUID batchIdUuid = request.getBatchId();
        BatchId batchId = batchIdUuid != null ? new BatchId(batchIdUuid) : null;
        
        Quantity quantity = new Quantity(
                new BigDecimal(request.getQuantity().getAmount()),
                new Unit(request.getQuantity().getUnit(), request.getQuantity().getUnit())
        );
        
        if (batchId != null) {
            // Batched instance
            instance = ProductInstance.batched(InstanceId.random(), productId, batchId, quantity);
        } else if (request.getSerialNumber() != null) {
            // Unique instance with serial number
            SerialNumber sn = new TextualSerialNumber(request.getSerialNumber());
            instance = ProductInstance.unique(InstanceId.random(), productId, sn);
        } else {
            // Identical/quantity-based instance
            instance = ProductInstance.identical(InstanceId.random(), productId, quantity);
        }
        
        inventoryFacade.addInstance(entry.id(), instance);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toInstanceResponse(instance));
    }

    /**
     * GET /api/inventory/instances/{instanceId} - Get instance by ID
     */
    @Override
    public ResponseEntity<InventoryInstanceResponse> getInventoryInstance(UUID instanceId, String xApiVersion) {
        getInstanceCounter.increment();
        
        // Search all entries for the instance
        for (InventoryEntry entry : inventoryFacade.allEntries()) {
            for (Instance instance : entry.instances()) {
                if (instance.id().value().equals(instanceId)) {
                    return ResponseEntity.ok(toInstanceResponse(instance));
                }
            }
        }
        
        return ResponseEntity.notFound().build();
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

    private BatchResponse toBatchResponse(Batch batch) {
        // Extract UUIDs from ProductIdentifier
        UUID productIdUuid = null;
        if (batch.batchOf() instanceof UuidProductIdentifier uuidId) {
            productIdUuid = uuidId.value();
        }
        
        BatchResponse response = new BatchResponse(
                batch.id().value(),
                productIdUuid,
                batch.name().toString(),
                null // quantity set below
        );
        
        BatchResponseQuantity qty = new BatchResponseQuantity();
        qty.setAmount(batch.quantityInBatch().amount().toString());
        qty.setUnit(batch.quantityInBatch().unit().toString());
        response.setQuantity(qty);
        
        // Use maybe* methods - convert to OffsetDateTime
        batch.maybeDateProduced().ifPresent(d -> 
                response.setDateProduced(d.atOffset(ZoneOffset.UTC)));
        batch.maybeBestBefore().ifPresent(b -> 
                response.setBestBefore(b.atOffset(ZoneOffset.UTC)));
        
        return response;
    }

    private InventoryInstanceResponse toInstanceResponse(Instance instance) {
        // Extract UUIDs
        UUID productIdUuid = null;
        if (instance.productId() instanceof UuidProductIdentifier uuidId) {
            productIdUuid = uuidId.value();
        }
        
        UUID batchIdUuid = instance.maybeBatchId().map(b -> b.value()).orElse(null);
        
        InventoryInstanceResponse response = new InventoryInstanceResponse(
                instance.id().value(),
                productIdUuid,
                null // quantity set below
        );
        
        if (batchIdUuid != null) {
            response.setBatchId(batchIdUuid);
        }
        instance.maybeSerialNumber().ifPresent(s -> response.setSerialNumber(s.value()));
        
        if (instance.maybeQuantity().isPresent()) {
            Quantity q = instance.maybeQuantity().get();
            com.acme.auctions.contract.model.BatchResponseQuantity qty = 
                    new com.acme.auctions.contract.model.BatchResponseQuantity();
            qty.setAmount(q.amount().toString());
            qty.setUnit(q.unit().toString());
            response.setQuantity(qty);
        }
        
        return response;
    }
}