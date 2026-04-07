package com.acme.auctions.core.lot.model;

import com.acme.auctions.inventory.InventoryEntryId;
import com.acme.auctions.core.auctioning.model.Price;

import java.util.Objects;

/**
 * Lot represents a decision to sell specific inventory items in a given mode.
 * It bridges Inventory (what we have physically) with Auctioning (the sales process).
 */
public class Lot {

    private final LotId id;
    private final String title;
    private final String description;
    private final InventoryEntryId inventoryEntryId;
    private final SellingMode sellingMode;
    private final Price reservePrice;

    private Lot(LotId id,
                String title,
                String description,
                InventoryEntryId inventoryEntryId,
                SellingMode sellingMode,
                Price reservePrice) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(title, "title must not be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(inventoryEntryId, "inventoryEntryId must not be null");
        Objects.requireNonNull(sellingMode, "sellingMode must not be null");
        Objects.requireNonNull(reservePrice, "reservePrice must not be null");
        this.id = id;
        this.title = title;
        this.description = description;
        this.inventoryEntryId = inventoryEntryId;
        this.sellingMode = sellingMode;
        this.reservePrice = reservePrice;
    }

    /**
     * Creates a lot for selling a single unique item (e.g., a car).
     */
    public static Lot createWhole(LotId id,
                                  String title,
                                  String description,
                                  InventoryEntryId inventoryEntryId,
                                  Price reservePrice) {
        return new Lot(id, title, description, inventoryEntryId, SellingMode.WHOLE, reservePrice);
    }

    /**
     * Creates a lot for selling divisible inventory (e.g., rice bags).
     */
    public static Lot createDivisible(LotId id,
                                      String title,
                                      String description,
                                      InventoryEntryId inventoryEntryId,
                                      Price reservePrice) {
        return new Lot(id, title, description, inventoryEntryId, SellingMode.DIVISIBLE, reservePrice);
    }

    public LotId id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public InventoryEntryId inventoryEntryId() { return inventoryEntryId; }
    public SellingMode sellingMode() { return sellingMode; }
    public Price reservePrice() { return reservePrice; }

    public boolean isDivisible() {
        return sellingMode == SellingMode.DIVISIBLE;
    }

    /**
     * Creates a draft lot for testing/legacy compatibility.
     */
    public static Lot singleProductDraft(String title) {
        return new Lot(
            LotId.newId(),
            title,
            "Draft lot",
            com.acme.auctions.inventory.InventoryEntryId.random(),
            SellingMode.WHOLE,
            new Price(java.math.BigDecimal.ONE, "PLN")
        );
    }

    @Override
    public String toString() {
        return "Lot{id=%s, title='%s', mode=%s, reserve=%s}".formatted(id, title, sellingMode, reservePrice);
    }
}
