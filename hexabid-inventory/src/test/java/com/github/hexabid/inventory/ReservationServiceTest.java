package com.github.hexabid.inventory;

import com.github.hexabid.product.BatchId;
import com.github.hexabid.product.ProductIdentifier;
import com.github.hexabid.quantity.Quantity;
import com.github.hexabid.quantity.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {

    private InventoryFacade inventoryFacade;
    private ReservationService reservationService;
    private InventoryEntry entry;
    private AuctionId auctionId;

    @BeforeEach
    void setUp() {
        inventoryFacade = new InventoryFacade();
        reservationService = new ReservationService(inventoryFacade);
        var productId = ProductIdentifier.randomUuid();
        entry = inventoryFacade.createInventoryEntry(new InventoryProduct(productId));
        // Add 100 kg of batched product
        var instance = ProductInstance.batched(
                InstanceId.random(), productId, BatchId.random(), Quantity.of(100, Unit.kilograms()));
        inventoryFacade.addInstance(entry.id(), instance);
        auctionId = AuctionId.next();
    }

    @Test
    void shouldReserveQuantity() {
        var reservation = reservationService.reserve(
                entry.id(), auctionId, Quantity.of(30, Unit.kilograms()));

        assertNotNull(reservation.id());
        assertEquals(entry.id(), reservation.entryId());
        assertEquals(auctionId, reservation.auctionId());
        assertEquals(Quantity.of(30, Unit.kilograms()), reservation.quantity());
        assertEquals(Reservation.ReservationStatus.ACTIVE, reservation.status());
    }

    @Test
    void shouldRejectReserveWhenInsufficientAvailability() {
        assertThrows(IllegalStateException.class, () ->
                reservationService.reserve(entry.id(), auctionId, Quantity.of(200, Unit.kilograms())));
    }

    @Test
    void shouldComputeCorrectAvailability() {
        reservationService.reserve(entry.id(), auctionId, Quantity.of(30, Unit.kilograms()));

        var availability = reservationService.getAvailability(entry.id());
        assertEquals(Quantity.of(100, Unit.kilograms()), availability.total());
        assertEquals(Quantity.of(30, Unit.kilograms()), availability.reserved());
        assertEquals(Quantity.of(70, Unit.kilograms()), availability.available());
    }

    @Test
    void shouldConfirmReservation() {
        var reservation = reservationService.reserve(
                entry.id(), auctionId, Quantity.of(50, Unit.kilograms()));
        reservationService.confirm(reservation.id());

        var found = reservationService.findById(reservation.id());
        assertTrue(found.isPresent());
        assertEquals(Reservation.ReservationStatus.CONFIRMED, found.get().status());
    }

    @Test
    void shouldCancelReservationAndFreeAvailability() {
        reservationService.reserve(entry.id(), auctionId, Quantity.of(30, Unit.kilograms()));

        var reservation2 = reservationService.reserve(
                entry.id(), AuctionId.next(), Quantity.of(40, Unit.kilograms()));

        var beforeCancel = reservationService.getAvailability(entry.id());
        assertEquals(Quantity.of(30, Unit.kilograms()), beforeCancel.available());

        reservationService.cancel(reservation2.id());

        var afterCancel = reservationService.getAvailability(entry.id());
        assertEquals(Quantity.of(70, Unit.kilograms()), afterCancel.available());
    }

    @Test
    void shouldFindReservationsByAuction() {
        var r1 = reservationService.reserve(entry.id(), auctionId, Quantity.of(30, Unit.kilograms()));
        var r2 = reservationService.reserve(entry.id(), auctionId, Quantity.of(20, Unit.kilograms()));

        var otherAuction = AuctionId.next();
        reservationService.reserve(entry.id(), otherAuction, Quantity.of(10, Unit.kilograms()));

        var forAuction = reservationService.findByAuction(auctionId);
        assertEquals(2, forAuction.size());
        assertTrue(forAuction.stream().anyMatch(r -> r.id().equals(r1.id())));
        assertTrue(forAuction.stream().anyMatch(r -> r.id().equals(r2.id())));
    }

    @Test
    void shouldRejectReserveForNonexistentEntry() {
        assertThrows(IllegalArgumentException.class, () ->
                reservationService.reserve(InventoryEntryId.random(), auctionId, Quantity.of(10, Unit.kilograms())));
    }

    @Test
    void shouldRecordMovement() {
        var instance = entry.instances().getFirst();
        reservationService.recordMovement(
                entry.id(), instance.id(),
                InventoryMovement.MovementType.SALE,
                Quantity.of(10, Unit.kilograms()),
                "Sold at auction " + auctionId);

        var movements = reservationService.getMovements(entry.id());
        assertEquals(1, movements.size());
        assertEquals(InventoryMovement.MovementType.SALE, movements.getFirst().type());
    }
}
