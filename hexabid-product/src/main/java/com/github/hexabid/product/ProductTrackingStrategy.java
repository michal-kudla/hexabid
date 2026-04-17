package com.github.hexabid.product;

/**
 * Defines how product instances are tracked and identified in the system.
 */
public enum ProductTrackingStrategy {

    /**
     * UNIQUE - One-of-a-kind product.
     * Example: Hetfield's guitar, da Vinci's painting, Jan Kowalski's car.
     */
    UNIQUE,

    /**
     * INDIVIDUALLY_TRACKED - Each instance uniquely identified.
     * Examples: iPhone, mortgage contract, parcel tracking.
     */
    INDIVIDUALLY_TRACKED,

    /**
     * BATCH_TRACKED - Tracked by production batch for quality control.
     * Examples: milk bottles, pharmaceuticals, food products, rice bags.
     */
    BATCH_TRACKED,

    /**
     * INDIVIDUALLY_AND_BATCH_TRACKED - Both individual and batch tracking.
     * Examples: TVs, smartphones (serial + batch for recalls).
     */
    INDIVIDUALLY_AND_BATCH_TRACKED,

    /**
     * IDENTICAL - Interchangeable items, may or may not create instances.
     * Examples: screws (bulk), rice bags (instance per bag), flour (bulk).
     */
    IDENTICAL;

    public boolean isTrackedIndividually() {
        return this == UNIQUE
            || this == INDIVIDUALLY_TRACKED
            || this == INDIVIDUALLY_AND_BATCH_TRACKED;
    }

    public boolean isTrackedByBatch() {
        return this == BATCH_TRACKED
            || this == INDIVIDUALLY_AND_BATCH_TRACKED;
    }

    public boolean requiresBothTrackingMethods() {
        return this == INDIVIDUALLY_AND_BATCH_TRACKED;
    }

    public boolean isInterchangeable() {
        return this == IDENTICAL;
    }
}
