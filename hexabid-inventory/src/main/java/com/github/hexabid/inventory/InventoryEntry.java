package com.github.hexabid.inventory;

import com.github.hexabid.product.ProductIdentifier;

import java.util.*;

/**
 * InventoryEntry is the aggregate root that maps a product to its instances.
 */
public class InventoryEntry {

    private final InventoryEntryId id;
    private final InventoryProduct product;
    private final Set<InstanceId> instances;
    private final Map<InstanceId, Instance> instanceDetails;

    private InventoryEntry(InventoryEntryId id, InventoryProduct product, Set<InstanceId> instances, Map<InstanceId, Instance> instanceDetails) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.product = Objects.requireNonNull(product, "product must not be null");
        this.instances = instances != null ? new HashSet<>(instances) : new HashSet<>();
        this.instanceDetails = instanceDetails != null ? new HashMap<>(instanceDetails) : new HashMap<>();
    }

    public static InventoryEntry create(InventoryProduct product) {
        return new InventoryEntry(InventoryEntryId.random(), product, new HashSet<>(), new HashMap<>());
    }

    public InventoryEntryId id() {
        return id;
    }

    public InventoryProduct product() {
        return product;
    }

    public ProductIdentifier productId() {
        return product.productId();
    }

    public void addInstance(Instance instance) {
        Objects.requireNonNull(instance, "instance must not be null");
        if (!instance.productId().equals(product.productId())) {
            throw new IllegalArgumentException(
                "Instance product %s does not match entry product %s".formatted(instance.productId(), product.productId()));
        }
        instances.add(instance.id());
        instanceDetails.put(instance.id(), instance);
    }

    public void removeInstance(InstanceId instanceId) {
        instances.remove(instanceId);
        instanceDetails.remove(instanceId);
    }

    public boolean hasInstance(InstanceId instanceId) {
        return instances.contains(instanceId);
    }

    public Optional<Instance> getInstance(InstanceId instanceId) {
        return Optional.ofNullable(instanceDetails.get(instanceId));
    }

    public Set<InstanceId> instanceIds() {
        return Set.copyOf(instances);
    }

    public List<Instance> instances() {
        return instances.stream()
            .map(instanceDetails::get)
            .filter(Objects::nonNull)
            .toList();
    }

    public int instanceCount() {
        return instances.size();
    }

    public boolean isEmpty() {
        return instances.isEmpty();
    }

    @Override
    public String toString() {
        return "InventoryEntry{id=%s, product=%s, instanceCount=%d}".formatted(id, product.productId(), instances.size());
    }
}
