package com.acme.auctions.product;

import com.acme.auctions.quantity.Unit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTypeTest {

    @Test
    void shouldCreateSimpleProductType() {
        ProductType product = ProductType.define(
            ProductIdentifier.randomUuid(),
            new ProductName("Test Product"),
            new ProductDescription("A test product")
        );

        assertNotNull(product.id());
        assertEquals("Test Product", product.name().value());
        assertEquals(ProductTrackingStrategy.IDENTICAL, product.trackingStrategy());
        assertEquals(Unit.pieces(), product.preferredUnit());
    }

    @Test
    void shouldCreateUniqueProductType() {
        ProductType product = ProductType.unique(
            ProductIdentifier.randomUuid(),
            new ProductName("Jan Kowalski's Car"),
            new ProductDescription("Seat Leon 1999 TDI")
        );

        assertEquals(ProductTrackingStrategy.UNIQUE, product.trackingStrategy());
        assertEquals(Unit.pieces(), product.preferredUnit());
    }

    @Test
    void shouldCreateBatchTrackedProductType() {
        ProductType product = ProductType.batchTracked(
            ProductIdentifier.randomUuid(),
            new ProductName("Jasmine Rice"),
            new ProductDescription("Premium jasmine rice"),
            Unit.kilograms()
        );

        assertEquals(ProductTrackingStrategy.BATCH_TRACKED, product.trackingStrategy());
        assertEquals(Unit.kilograms(), product.preferredUnit());
    }

    @Test
    void shouldCreateIndividuallyTrackedProductType() {
        ProductType product = ProductType.individuallyTracked(
            ProductIdentifier.randomUuid(),
            new ProductName("iPhone 15 Pro"),
            new ProductDescription("iPhone 15 Pro 256GB"),
            Unit.pieces()
        );

        assertEquals(ProductTrackingStrategy.INDIVIDUALLY_TRACKED, product.trackingStrategy());
    }

    @Test
    void shouldCreateIndividuallyAndBatchTrackedProductType() {
        ProductType product = ProductType.individuallyAndBatchTracked(
            ProductIdentifier.randomUuid(),
            new ProductName("Samsung TV"),
            new ProductDescription("Samsung 55\" QLED TV"),
            Unit.pieces()
        );

        assertEquals(ProductTrackingStrategy.INDIVIDUALLY_AND_BATCH_TRACKED, product.trackingStrategy());
    }

    @Test
    void shouldCreateIdenticalProductType() {
        ProductType product = ProductType.identical(
            ProductIdentifier.randomUuid(),
            new ProductName("Screws M4"),
            new ProductDescription("M4 screws, zinc plated"),
            Unit.pieces()
        );

        assertEquals(ProductTrackingStrategy.IDENTICAL, product.trackingStrategy());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () ->
            ProductType.define(null, new ProductName("Test"), new ProductDescription("Test")));
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () ->
            ProductType.define(ProductIdentifier.randomUuid(), null, new ProductDescription("Test")));
    }

    @Test
    void shouldRejectNullDescription() {
        assertThrows(NullPointerException.class, () ->
            ProductType.define(ProductIdentifier.randomUuid(), new ProductName("Test"), null));
    }

    @Test
    void shouldBeApplicableForAnyContextWhenAlwaysTrue() {
        ProductType product = ProductType.unique(
            ProductIdentifier.randomUuid(),
            new ProductName("Unique Item"),
            new ProductDescription("A unique item")
        );

        assertTrue(product.isApplicableFor(ApplicabilityContext.empty()));
        assertTrue(product.isApplicableFor(ApplicabilityContext.of("key", "value")));
    }
}
