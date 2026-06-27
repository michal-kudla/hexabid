package com.github.hexabid.inventory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Facade for the Inventory context.
 * Manages inventory entries and their instances.
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
 * @see <a href="doc/domain-modeling/domain-modeling-plan.md">Domain Modeling Plan</a>
 * @see <a href="doc/backend/inventory/README.md">Inventory Backend Documentation</a>
 */
public class InventoryFacade {

    private final Map<InventoryEntryId, InventoryEntry> entries = new ConcurrentHashMap<>();

    public InventoryEntry createInventoryEntry(InventoryProduct product) {
        InventoryEntry entry = InventoryEntry.create(product);
        entries.put(entry.id(), entry);
        return entry;
    }

    public Optional<InventoryEntry> findEntry(InventoryEntryId id) {
        return Optional.ofNullable(entries.get(id));
    }

    public List<InventoryEntry> entriesForProduct(com.github.hexabid.product.ProductIdentifier productId) {
        return entries.values().stream()
            .filter(e -> e.productId().equals(productId))
            .toList();
    }

    public List<InventoryEntry> allEntries() {
        return List.copyOf(entries.values());
    }

    public void addInstance(InventoryEntryId entryId, Instance instance) {
        InventoryEntry entry = entries.get(entryId);
        if (entry == null) {
            throw new IllegalArgumentException("InventoryEntry not found: " + entryId);
        }
        entry.addInstance(instance);
    }

    public void removeInstance(InventoryEntryId entryId, InstanceId instanceId) {
        InventoryEntry entry = entries.get(entryId);
        if (entry == null) {
            throw new IllegalArgumentException("InventoryEntry not found: " + entryId);
        }
        entry.removeInstance(instanceId);
    }

    public Optional<Instance> getInstance(InventoryEntryId entryId, InstanceId instanceId) {
        InventoryEntry entry = entries.get(entryId);
        if (entry == null) {
            return Optional.empty();
        }
        return entry.getInstance(instanceId);
    }
}
