package com.github.hexabid.product;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Facade for the Product context.
 * Orchestrates product creation, retrieval, and catalog management.
 * Follows the pattern from archetypyoprogramowania.
 *
 * ARCHETYP: Product (M02 - Katalog produktów)
 *
 * Product to kontekst "co to jest" - definicja produktu niezależna od stanu fizycznego.
 * Ścieżka danych:
 * 1. ProductType (definicja) → ProductFacade
 * 2. PackageType (pakiet) → ProductFacade
 * 3. CatalogEntry (oferta handlowa) → ProductFacade
 * 4. Batch (partia produkcyjna) → ProductFacade
 *
 * @see <a href="doc/domain-modeling/domain-modeling-plan.md">Domain Modeling Plan</a>
 * @see <a href="doc/backend/product/README.md">Product Backend Documentation</a>
 */
public class ProductFacade {

    private final Map<ProductIdentifier, ProductType> productTypes = new ConcurrentHashMap<>();
    private final Map<ProductIdentifier, PackageType> packageTypes = new ConcurrentHashMap<>();
    private final Map<CatalogEntryId, CatalogEntry> catalogEntries = new ConcurrentHashMap<>();
    private final Map<BatchId, Batch> batches = new ConcurrentHashMap<>();

    // === ProductType management ===

    public ProductType createProductType(ProductType productType) {
        Objects.requireNonNull(productType, "productType must not be null");
        productTypes.put(productType.id(), productType);
        return productType;
    }

    public Optional<ProductType> findProductType(ProductIdentifier id) {
        return Optional.ofNullable(productTypes.get(id));
    }

    public List<ProductType> allProductTypes() {
        return List.copyOf(productTypes.values());
    }

    // === PackageType management ===

    public PackageType createPackageType(PackageType packageType) {
        Objects.requireNonNull(packageType, "packageType must not be null");
        packageTypes.put(packageType.id(), packageType);
        return packageType;
    }

    public Optional<PackageType> findPackageType(ProductIdentifier id) {
        return Optional.ofNullable(packageTypes.get(id));
    }

    public List<PackageType> allPackageTypes() {
        return List.copyOf(packageTypes.values());
    }

    // === CatalogEntry management ===

    public CatalogEntry publishCatalogEntry(CatalogEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        catalogEntries.put(entry.id(), entry);
        return entry;
    }

    public Optional<CatalogEntry> findCatalogEntry(CatalogEntryId id) {
        return Optional.ofNullable(catalogEntries.get(id));
    }

    public List<CatalogEntry> availableCatalogEntriesAt(java.time.LocalDate date) {
        return catalogEntries.values().stream()
            .filter(e -> e.isAvailableAt(date))
            .toList();
    }

    public List<CatalogEntry> catalogEntriesInCategory(String category) {
        return catalogEntries.values().stream()
            .filter(e -> e.isInCategory(category))
            .toList();
    }

    public List<CatalogEntry> allCatalogEntries() {
        return List.copyOf(catalogEntries.values());
    }

    // === Batch management ===

    public Batch createBatch(Batch batch) {
        Objects.requireNonNull(batch, "batch must not be null");
        batches.put(batch.id(), batch);
        return batch;
    }

    public Optional<Batch> findBatch(BatchId id) {
        return Optional.ofNullable(batches.get(id));
    }

    public List<Batch> batchesOf(ProductIdentifier productIdentifier) {
        return batches.values().stream()
            .filter(b -> b.batchOf().equals(productIdentifier))
            .toList();
    }

    public List<Batch> allBatches() {
        return List.copyOf(batches.values());
    }

    // === Validation ===

    public boolean isProductApplicable(ProductIdentifier productId, ApplicabilityContext context) {
        ProductType type = productTypes.get(productId);
        if (type != null) {
            return type.isApplicableFor(context);
        }
        PackageType pkg = packageTypes.get(productId);
        if (pkg != null) {
            return pkg.isApplicableFor(context);
        }
        return false;
    }

    public boolean validatePackageSelection(ProductIdentifier packageId, List<SelectedProduct> selection) {
        PackageType pkg = packageTypes.get(packageId);
        if (pkg == null) {
            return false;
        }
        return pkg.validateSelection(selection);
    }
}
