package com.github.hexabid.inventory;

import com.github.hexabid.quantity.Quantity;
import java.time.Instant;
import java.util.Objects;

public final class Reservation {

    private final ReservationId id;
    private final InventoryEntryId entryId;
    private final AuctionId auctionId;
    private final Quantity quantity;
    private final Instant createdAt;
    private ReservationStatus status;

    public Reservation(InventoryEntryId entryId, AuctionId auctionId, Quantity quantity) {
        this.id = ReservationId.next();
        this.entryId = Objects.requireNonNull(entryId, "entryId must not be null");
        this.auctionId = Objects.requireNonNull(auctionId, "auctionId must not be null");
        this.quantity = Objects.requireNonNull(quantity, "quantity must not be null");
        this.createdAt = Instant.now();
        this.status = ReservationStatus.ACTIVE;
    }

    public ReservationId id() { return id; }
    public InventoryEntryId entryId() { return entryId; }
    public AuctionId auctionId() { return auctionId; }
    public Quantity quantity() { return quantity; }
    public Instant createdAt() { return createdAt; }
    public ReservationStatus status() { return status; }

    public void confirm() {
        if (status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Cannot confirm reservation in status: " + status);
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        if (status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Cannot cancel reservation in status: " + status);
        }
        this.status = ReservationStatus.CANCELLED;
    }

    public void expire() {
        if (status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Cannot expire reservation in status: " + status);
        }
        this.status = ReservationStatus.EXPIRED;
    }

    public boolean isActive() {
        return status == ReservationStatus.ACTIVE;
    }

    public enum ReservationStatus {
        ACTIVE, CONFIRMED, CANCELLED, EXPIRED
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Reservation that && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", entry=" + entryId + ", auction=" + auctionId + ", status=" + status + "}";
    }
}
