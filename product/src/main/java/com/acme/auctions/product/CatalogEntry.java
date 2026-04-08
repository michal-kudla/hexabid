package com.acme.auctions.product;

import java.time.LocalDate;
import java.util.*;

/**
 * CatalogEntry - commercial offering position.
 * Represents what the organization currently offers to customers.
 *
 * Product (ProductType or PackageType) says what something IS (business/operational definition).
 * CatalogEntry says that something is FOR SALE (commercial availability).
 */
public class CatalogEntry {

    private final CatalogEntryId id;
    private final String displayName;
    private final String description;
    private final Product product;
    private final Set<String> categories;
    private final Validity validity;
    private final Map<String, String> metadata;

    private CatalogEntry(CatalogEntryId id,
                         String displayName,
                         String description,
                         Product product,
                         Set<String> categories,
                         Validity validity,
                         Map<String, String> metadata) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        Objects.requireNonNull(description, "description must not be null");
        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        Objects.requireNonNull(product, "product must not be null");
        Objects.requireNonNull(validity, "validity must not be null");
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.product = product;
        this.categories = categories != null ? Set.copyOf(categories) : Set.of();
        this.validity = validity;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public static Builder builder() {
        return new Builder();
    }

    public CatalogEntryId id() { return id; }
    public String displayName() { return displayName; }
    public String description() { return description; }
    public Product product() { return product; }
    public Set<String> categories() { return categories; }
    public Validity validity() { return validity; }
    public Map<String, String> metadata() { return metadata; }

    public boolean isAvailableAt(LocalDate date) {
        return validity.isValidAt(date);
    }

    public boolean isInCategory(String category) {
        return categories.contains(category);
    }

    public Optional<String> maybeMetadata(String key) {
        return Optional.ofNullable(metadata.get(key));
    }

    public String metadataOrDefault(String key, String defaultValue) {
        return metadata.getOrDefault(key, defaultValue);
    }

    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    public CatalogEntry withValidity(Validity newValidity) {
        return new CatalogEntry(id, displayName, description, product, categories, newValidity, metadata);
    }

    public CatalogEntry withMetadata(Map<String, String> newMetadata) {
        return new CatalogEntry(id, displayName, description, product, categories, validity, newMetadata);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatalogEntry that = (CatalogEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CatalogEntry{id=%s, displayName='%s', product=%s, categories=%s, validity=%s}"
            .formatted(id, displayName, product.name(), categories, validity);
    }

    public static class Builder {
        private CatalogEntryId id;
        private String displayName;
        private String description;
        private Product product;
        private Set<String> categories = new HashSet<>();
        private Validity validity;
        private Map<String, String> metadata = new HashMap<>();

        public Builder id(CatalogEntryId id) { this.id = id; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder product(Product product) { this.product = product; return this; }
        public Builder categories(Set<String> categories) { this.categories = new HashSet<>(categories); return this; }
        public Builder category(String category) { this.categories.add(category); return this; }
        public Builder validity(Validity validity) { this.validity = validity; return this; }
        public Builder metadata(Map<String, String> metadata) { this.metadata = new HashMap<>(metadata); return this; }
        public Builder withMetadata(String key, String value) { this.metadata.put(key, value); return this; }

        public CatalogEntry build() {
            return new CatalogEntry(id, displayName, description, product, categories, validity, metadata);
        }
    }
}
