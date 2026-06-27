package com.github.hexabid.inventory;

import com.github.hexabid.quantity.Quantity;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ReservationService {

    private final InventoryFacade inventoryFacade;
    private final Map<ReservationId, Reservation> reservations = new ConcurrentHashMap<>();
    private final Map<InventoryEntryId, List<InventoryMovement>> movements = new ConcurrentHashMap<>();

    public ReservationService(InventoryFacade inventoryFacade) {
        this.inventoryFacade = Objects.requireNonNull(inventoryFacade, "inventoryFacade must not be null");
    }

    public Reservation reserve(InventoryEntryId entryId, AuctionId auctionId, Quantity quantity) {
        inventoryFacade.findEntry(entryId)
                .orElseThrow(() -> new IllegalArgumentException("InventoryEntry not found: " + entryId));

        var availability = getAvailability(entryId);
        if (quantity.isGreaterThan(availability.available())) {
            throw new IllegalStateException(
                    "Insufficient availability: requested " + quantity + ", available " + availability.available());
        }

        var reservation = new Reservation(entryId, auctionId, quantity);
        reservations.put(reservation.id(), reservation);
        return reservation;
    }

    public void confirm(ReservationId reservationId) {
        var reservation = reservations.get(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found: " + reservationId);
        }
        reservation.confirm();
    }

    public void cancel(ReservationId reservationId) {
        var reservation = reservations.get(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found: " + reservationId);
        }
        reservation.cancel();
    }

    public Optional<Reservation> findById(ReservationId id) {
        return Optional.ofNullable(reservations.get(id));
    }

    public List<Reservation> findByAuction(AuctionId auctionId) {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        return reservations.values().stream()
                .filter(r -> auctionId.equals(r.auctionId()))
                .toList();
    }

    public List<Reservation> findByEntry(InventoryEntryId entryId) {
        Objects.requireNonNull(entryId, "entryId must not be null");
        return reservations.values().stream()
                .filter(r -> entryId.equals(r.entryId()))
                .toList();
    }

    public List<Reservation> allReservations() {
        return List.copyOf(reservations.values());
    }

    public Availability getAvailability(InventoryEntryId entryId) {
        var entry = inventoryFacade.findEntry(entryId)
                .orElseThrow(() -> new IllegalArgumentException("InventoryEntry not found: " + entryId));

        var instances = entry.instances();
        if (instances.isEmpty()) {
            return Availability.of(Quantity.of(0, com.github.hexabid.quantity.Unit.pieces()), Quantity.of(0, com.github.hexabid.quantity.Unit.pieces()));
        }

        var unit = instances.getFirst().effectiveQuantity().unit();
        var total = instances.stream()
                .map(Instance::effectiveQuantity)
                .map(Quantity::amount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        var reserved = reservations.values().stream()
                .filter(r -> r.entryId().equals(entry.id()) && r.isActive())
                .map(Reservation::quantity)
                .map(Quantity::amount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return Availability.of(Quantity.of(total, unit), Quantity.of(reserved, unit));
    }

    public void recordMovement(InventoryEntryId entryId, InstanceId instanceId,
                                InventoryMovement.MovementType type, Quantity quantity, String reason) {
        var instance = inventoryFacade.getInstance(entryId, instanceId)
                .orElseThrow(() -> new IllegalArgumentException("Instance not found: " + instanceId + " in entry " + entryId));
        var movement = new InventoryMovement(entryId, instanceId, type, quantity, reason);
        movements.computeIfAbsent(entryId, k -> new ArrayList<>()).add(movement);
    }

    public List<InventoryMovement> getMovements(InventoryEntryId entryId) {
        return movements.getOrDefault(entryId, List.of());
    }

    public List<InventoryMovement> allMovements() {
        return movements.values().stream().flatMap(Collection::stream).toList();
    }
}
