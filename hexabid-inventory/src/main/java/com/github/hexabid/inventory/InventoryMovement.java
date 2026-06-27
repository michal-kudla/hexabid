package com.github.hexabid.inventory;

import com.github.hexabid.quantity.Quantity;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class InventoryMovement {

    private final UUID id;
    private final InventoryEntryId entryId;
    private final InstanceId instanceId;
    private final MovementType type;
    private final Quantity quantity;
    private final String reason;
    private final Instant occurredAt;

    public InventoryMovement(InventoryEntryId entryId, InstanceId instanceId,
                             MovementType type, Quantity quantity, String reason) {
        this.id = UUID.randomUUID();
        this.entryId = Objects.requireNonNull(entryId, "entryId must not be null");
        this.instanceId = Objects.requireNonNull(instanceId, "instanceId must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.quantity = Objects.requireNonNull(quantity, "quantity must not be null");
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
        this.occurredAt = Instant.now();
    }

    public UUID id() { return id; }
    public InventoryEntryId entryId() { return entryId; }
    public InstanceId instanceId() { return instanceId; }
    public MovementType type() { return type; }
    public Quantity quantity() { return quantity; }
    public String reason() { return reason; }
    public Instant occurredAt() { return occurredAt; }

    public enum MovementType {
        RECEIPT,
        SALE,
        RETURN,
        TRANSFER,
        ADJUSTMENT,
        LOSS
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof InventoryMovement that && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "InventoryMovement{id=" + id + ", entry=" + entryId + ", type=" + type + ", quantity=" + quantity + "}";
    }
}
