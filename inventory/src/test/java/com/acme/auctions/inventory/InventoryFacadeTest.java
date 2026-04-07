package com.acme.auctions.inventory;

import com.acme.auctions.product.BatchId;
import com.acme.auctions.product.ProductIdentifier;
import com.acme.auctions.product.TextualSerialNumber;
import com.acme.auctions.quantity.Quantity;
import com.acme.auctions.quantity.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InventoryFacadeTest {

    private InventoryFacade facade;

    @BeforeEach
    void setUp() {
        facade = new InventoryFacade();
    }

    @Test
    void shouldCreateInventoryEntry() {
        ProductIdentifier productId = ProductIdentifier.randomUuid();
        InventoryEntry entry = facade.createInventoryEntry(new InventoryProduct(productId));

        assertNotNull(entry.id());
        assertEquals(productId, entry.productId());
        assertTrue(entry.isEmpty());
    }

    @Test
    void shouldFindEntryById() {
        ProductIdentifier productId = ProductIdentifier.randomUuid();
        InventoryEntry entry = facade.createInventoryEntry(new InventoryProduct(productId));

        Optional<InventoryEntry> found = facade.findEntry(entry.id());
        assertTrue(found.isPresent());
        assertEquals(entry.id(), found.get().id());
    }

    @Test
    void shouldReturnEmptyWhenEntryNotFound() {
        Optional<InventoryEntry> found = facade.findEntry(InventoryEntryId.random());
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFilterEntriesByProduct() {
        ProductIdentifier productId = ProductIdentifier.randomUuid();
        facade.createInventoryEntry(new InventoryProduct(productId));
        facade.createInventoryEntry(new InventoryProduct(ProductIdentifier.randomUuid()));

        assertEquals(1, facade.entriesForProduct(productId).size());
    }

    @Test
    void shouldAddInstanceToEntry() {
        ProductIdentifier productId = ProductIdentifier.randomUuid();
        InventoryEntry entry = facade.createInventoryEntry(new InventoryProduct(productId));

        Instance instance = ProductInstance.unique(
            InstanceId.random(),
            productId,
            new TextualSerialNumber("SN-001")
        );

        facade.addInstance(entry.id(), instance);

        assertEquals(1, entry.instanceCount());
        assertTrue(entry.hasInstance(instance.id()));
    }

    @Test
    void shouldRejectInstanceWithMismatchedProduct() {
        ProductIdentifier productId = ProductIdentifier.randomUuid();
        InventoryEntry entry = facade.createInventoryEntry(new InventoryProduct(productId));

        Instance instance = ProductInstance.unique(
            InstanceId.random(),
            ProductIdentifier.randomUuid(),
            new TextualSerialNumber("SN-001")
        );

        assertThrows(IllegalArgumentException.class, () -> facade.addInstance(entry.id(), instance));
    }

    @Test
    void shouldRemoveInstanceFromEntry() {
        ProductIdentifier productId = ProductIdentifier.randomUuid();
        InventoryEntry entry = facade.createInventoryEntry(new InventoryProduct(productId));

        InstanceId instanceId = InstanceId.random();
        Instance instance = ProductInstance.unique(instanceId, productId, new TextualSerialNumber("SN-001"));
        facade.addInstance(entry.id(), instance);

        facade.removeInstance(entry.id(), instanceId);

        assertEquals(0, entry.instanceCount());
        assertFalse(entry.hasInstance(instanceId));
    }

    @Test
    void shouldGetInstanceDetails() {
        ProductIdentifier productId = ProductIdentifier.randomUuid();
        InventoryEntry entry = facade.createInventoryEntry(new InventoryProduct(productId));

        InstanceId instanceId = InstanceId.random();
        Instance instance = ProductInstance.batched(
            instanceId, productId, BatchId.random(), Quantity.of(100, Unit.kilograms()));
        facade.addInstance(entry.id(), instance);

        Optional<Instance> found = facade.getInstance(entry.id(), instanceId);
        assertTrue(found.isPresent());
        assertEquals(Quantity.of(100, Unit.kilograms()), found.get().effectiveQuantity());
    }

    @Test
    void shouldListAllEntries() {
        facade.createInventoryEntry(new InventoryProduct(ProductIdentifier.randomUuid()));
        facade.createInventoryEntry(new InventoryProduct(ProductIdentifier.randomUuid()));

        assertEquals(2, facade.allEntries().size());
    }
}
