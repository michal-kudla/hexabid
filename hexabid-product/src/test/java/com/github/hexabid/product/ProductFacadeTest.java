package com.github.hexabid.product;

import com.github.hexabid.quantity.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductFacadeTest {

    private ProductFacade facade;

    @BeforeEach
    void setUp() {
        facade = new ProductFacade();
    }

    @Test
    void shouldCreateAndFindProductType() {
        ProductType productType = ProductType.unique(
            ProductIdentifier.randomUuid(),
            new ProductName("Jan Kowalski's Car"),
            new ProductDescription("Seat Leon 1999 TDI")
        );

        facade.createProductType(productType);

        Optional<ProductType> found = facade.findProductType(productType.id());
        assertTrue(found.isPresent());
        assertEquals(productType, found.get());
    }

    @Test
    void shouldReturnEmptyWhenProductTypeNotFound() {
        Optional<ProductType> found = facade.findProductType(ProductIdentifier.randomUuid());
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldListAllProductTypes() {
        ProductType p1 = ProductType.define(ProductIdentifier.randomUuid(), new ProductName("P1"), new ProductDescription("D1"));
        ProductType p2 = ProductType.define(ProductIdentifier.randomUuid(), new ProductName("P2"), new ProductDescription("D2"));

        facade.createProductType(p1);
        facade.createProductType(p2);

        assertEquals(2, facade.allProductTypes().size());
    }

    @Test
    void shouldCreateAndFindPackageType() {
        ProductIdentifier laptopId = ProductIdentifier.randomUuid();
        ProductIdentifier bagId = ProductIdentifier.randomUuid();

        PackageStructure structure = new PackageStructure(
            List.of(
                ProductSet.singleOf("Laptop", laptopId),
                ProductSet.singleOf("Bag", bagId)
            ),
            List.of(
                SelectionRule.single(ProductSet.singleOf("Laptop", laptopId)),
                SelectionRule.optional(ProductSet.singleOf("Bag", bagId))
            )
        );

        PackageType packageType = PackageType.define(
            ProductIdentifier.randomUuid(),
            new ProductName("Laptop Bundle"),
            new ProductDescription("Laptop with accessories"),
            structure
        );

        facade.createPackageType(packageType);

        Optional<PackageType> found = facade.findPackageType(packageType.id());
        assertTrue(found.isPresent());
    }

    @Test
    void shouldPublishAndFindCatalogEntry() {
        ProductType productType = ProductType.unique(
            ProductIdentifier.randomUuid(),
            new ProductName("Unique Item"),
            new ProductDescription("A unique item")
        );
        facade.createProductType(productType);

        CatalogEntry entry = CatalogEntry.builder()
            .id(CatalogEntryId.random())
            .displayName("Unique Item - For Sale")
            .description("Buy this unique item")
            .product(productType)
            .category("unique")
            .validity(Validity.from(LocalDate.now()))
            .build();

        facade.publishCatalogEntry(entry);

        Optional<CatalogEntry> found = facade.findCatalogEntry(entry.id());
        assertTrue(found.isPresent());
    }

    @Test
    void shouldFilterCatalogEntriesByAvailability() {
        ProductType productType = ProductType.define(
            ProductIdentifier.randomUuid(),
            new ProductName("Seasonal Item"),
            new ProductDescription("Available only in summer")
        );
        facade.createProductType(productType);

        CatalogEntry entry = CatalogEntry.builder()
            .id(CatalogEntryId.random())
            .displayName("Seasonal Item")
            .description("Summer only")
            .product(productType)
            .validity(Validity.between(LocalDate.of(2025, 6, 1), LocalDate.of(2025, 8, 31)))
            .build();

        facade.publishCatalogEntry(entry);

        assertEquals(1, facade.availableCatalogEntriesAt(LocalDate.of(2025, 7, 1)).size());
        assertEquals(0, facade.availableCatalogEntriesAt(LocalDate.of(2025, 1, 1)).size());
    }

    @Test
    void shouldFilterCatalogEntriesByCategory() {
        ProductType productType = ProductType.define(
            ProductIdentifier.randomUuid(),
            new ProductName("Item"),
            new ProductDescription("An item")
        );
        facade.createProductType(productType);

        CatalogEntry entry = CatalogEntry.builder()
            .id(CatalogEntryId.random())
            .displayName("Item")
            .description("An item")
            .product(productType)
            .category("motoryzacja")
            .validity(Validity.from(LocalDate.now()))
            .build();

        facade.publishCatalogEntry(entry);

        assertEquals(1, facade.catalogEntriesInCategory("motoryzacja").size());
        assertEquals(0, facade.catalogEntriesInCategory("elektronika").size());
    }

    @Test
    void shouldCreateAndFindBatch() {
        ProductType riceType = ProductType.batchTracked(
            ProductIdentifier.randomUuid(),
            new ProductName("Jasmine Rice"),
            new ProductDescription("Premium jasmine rice"),
            Unit.kilograms()
        );
        facade.createProductType(riceType);

        Batch batch = Batch.builder()
            .id(BatchId.random())
            .name(new BatchName("TH-2024-001"))
            .batchOf(riceType.id())
            .quantityInBatch(com.github.hexabid.quantity.Quantity.of(2000, Unit.kilograms()))
            .build();

        facade.createBatch(batch);

        Optional<Batch> found = facade.findBatch(batch.id());
        assertTrue(found.isPresent());

        List<Batch> batchesOfRice = facade.batchesOf(riceType.id());
        assertEquals(1, batchesOfRice.size());
    }

    @Test
    void shouldValidatePackageSelection() {
        ProductIdentifier laptopId = ProductIdentifier.randomUuid();
        ProductIdentifier bagId = ProductIdentifier.randomUuid();

        PackageStructure structure = new PackageStructure(
            List.of(
                ProductSet.singleOf("Laptop", laptopId),
                ProductSet.singleOf("Bag", bagId)
            ),
            List.of(
                SelectionRule.single(ProductSet.singleOf("Laptop", laptopId)),
                SelectionRule.optional(ProductSet.singleOf("Bag", bagId))
            )
        );

        PackageType packageType = PackageType.define(
            ProductIdentifier.randomUuid(),
            new ProductName("Laptop Bundle"),
            new ProductDescription("Laptop with accessories"),
            structure
        );
        facade.createPackageType(packageType);

        List<SelectedProduct> validSelection = List.of(
            new SelectedProduct(laptopId, 1),
            new SelectedProduct(bagId, 1)
        );
        assertTrue(facade.validatePackageSelection(packageType.id(), validSelection));

        List<SelectedProduct> invalidSelection = List.of(
            new SelectedProduct(bagId, 1)
        );
        assertFalse(facade.validatePackageSelection(packageType.id(), invalidSelection));
    }

    @Test
    void shouldCheckProductApplicability() {
        ProductType productType = ProductType.unique(
            ProductIdentifier.randomUuid(),
            new ProductName("Adult Only Item"),
            new ProductDescription("Only for adults")
        );
        facade.createProductType(productType);

        assertTrue(facade.isProductApplicable(productType.id(), ApplicabilityContext.empty()));
    }
}
