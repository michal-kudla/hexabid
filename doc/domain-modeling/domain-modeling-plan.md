# Plan modelowania domeny aukcyjnej Hexabid

## Kontekst biznesowy

System aukcyjny Hexabid musi obsłużyć dwa fundamentalne scenariusze sprzedaży:

1. **Unikalne dobra** — "Samochód Jana Kowalskiego, Seat Leon 1999r TDI" — jeden egzemplarz, jedna wygrana licytacja
2. **Produkty podzielne** — kontener ryżu, który można sprzedać jako całość LUB podzielić na worki po 100 kg z wieloma kupującymi

## Źródła wiedzy

- **Archetypy oprogramowania** (PDF + TXT w `doc/archetypyoprogramowania/`)
- **Repozytorium referencyjne** — https://github.com/archetypy-oprogramowania/archetypes
  - Moduły: `product`, `inventory`, `ordering`, `pricing`, `accounting`, `party`, `quantity`, `rules`, `graphs`, `planvsexecution`, `common`
- **Obecny model** — `auctions-core/src/main/java/com/acme/auctions/core/`

## Analiza obecnego modelu (stan przed)

```
Product (record)
  └── productId: ProductId
  └── name: String

Lot (record)
  └── title: String
  └── product: Product  ← zbyt uproszczone

Auction (aggregate root)
  └── id: AuctionId
  └── sellerId: PartyId
  └── lot: Lot
  └── startingPrice: Price
  └── endsAt: Instant
  └── status: AuctionStatus
  └── biddingHistory: List<Bid>

Party (record)
  └── partyId: PartyId
  └── displayName: String
  └── roles: Set<PartyRole>

Bid (record)
  └── bidderId: PartyId
  └── amount: Price
  └── placedAt: Instant
```

Problemy:
- `Product` nie rozróżnia typu od instancji — nie ma `ProductType` vs `ProductInstance`
- Brak strategii śledzenia — nie można wyrazić "jeden na świecie" vs "partia 1000 worków"
- Brak partii (`Batch`) — nie można śledzić pochodzenia towaru
- `Lot` bezpośrednio referuje `Product` — nie ma warstwy inwentarza
- Brak katalogu ofert (`CatalogEntry`) — nie można zarządzać dostępnością
- Brak pakietów (`PackageType`) — nie można łączyć produktów w zestawy

---

## Planowany model domeny

### Warstwa 1: Product (definicja — "co to jest")

```
Product (interface — composite pattern)
  ├── ProductType (leaf — pojedynczy produkt)
  │     ├── id: ProductIdentifier
  │     ├── name: ProductName
  │     ├── description: ProductDescription
  │     ├── preferredUnit: Unit
  │     ├── trackingStrategy: ProductTrackingStrategy
  │     ├── featureTypes: ProductFeatureTypes
  │     ├── metadata: ProductMetadata
  │     └── applicabilityConstraint: ApplicabilityConstraint
  │
  └── PackageType (composite — pakiet produktów)
        ├── id: ProductIdentifier
        ├── name: ProductName
        ├── description: ProductDescription
        ├── trackingStrategy: ProductTrackingStrategy
        ├── metadata: ProductMetadata
        ├── applicabilityConstraint: ApplicabilityConstraint
        └── structure: PackageStructure
              ├── productSets: List<ProductSet>
              └── selectionRules: List<SelectionRule>
```

#### ProductTrackingStrategy

```java
enum ProductTrackingStrategy {
    UNIQUE,                          // jeden na świecie — Hetfield's guitar
    INDIVIDUALLY_TRACKED,            // każdy egzemplarz osobno — iPhone
    BATCH_TRACKED,                   // śledzenie po partii — mleko, ryż
    INDIVIDUALLY_AND_BATCH_TRACKED,  // serial + batch — telewizory
    IDENTICAL                        // zamienne — śruby luzem
}
```

#### ProductFeatureType — dynamiczne cechy

```
ProductFeatureType
  ├── name: String
  └── constraint: FeatureValueConstraint
        ├── AllowedValuesConstraint   // kolor: czerwony, zielony, niebieski
        ├── NumericRangeConstraint    // rok produkcji: 1990-2024
        ├── DecimalRangeConstraint    // waga: 0.1-125.0 kg
        ├── DateRangeConstraint       // data ważności
        ├── RegexConstraint           // kod partii: ABC-2024-PL
        └── Unconstrained             // dowolny tekst
```

#### ProductFeatureInstance — konkretne wartości

```
ProductFeatureInstance
  ├── type: ProductFeatureType
  └── value: Object  // zwalidowane przez constraint
```

#### ProductRelationship — relacje między produktami

```
ProductRelationship
  ├── id: ProductRelationshipId
  ├── from: ProductIdentifier
  ├── to: ProductIdentifier
  └── type: ProductRelationshipType
        ├── UPGRADABLE_TO
        ├── SUBSTITUTED_BY
        ├── REPLACED_BY
        ├── COMPLEMENTED_BY
        ├── COMPATIBLE_WITH
        └── INCOMPATIBLE_WITH
```

#### CatalogEntry — oferta handlowa

```
CatalogEntry
  ├── id: CatalogEntryId
  ├── displayName: String        // nazwa marketingowa
  ├── description: String        // tekst sprzedażowy
  ├── product: Product           // referencja do ProductType lub PackageType
  ├── categories: Set<String>
  ├── validity: Validity         // od-do dostępności
  └── metadata: Map<String, String>
```

---

### Warstwa 2: Inventory (inwentarz — "co mamy fizycznie")

```
InventoryEntry (aggregate root)
  ├── id: InventoryEntryId
  ├── product: InventoryProduct    // referencja do ProductType
  ├── instances: Set<InstanceId>   // wszystkie egzemplarze
  └── instanceToResource: Map<InstanceId, ResourceId>

Instance (interface)
  ├── ProductInstance (leaf)
  │     ├── id: InstanceId
  │     ├── productType: ProductType
  │     ├── serialNumber: SerialNumber?
  │     ├── batchId: BatchId?
  │     ├── quantity: Quantity?
  │     └── features: ProductFeatureInstances
  │
  └── PackageInstance (composite)
        ├── id: InstanceId
        ├── packageType: PackageType
        ├── selectedInstances: List<SelectedInstance>
        └── features: ProductFeatureInstances
```

#### Batch — partia produkcyjna

```
Batch
  ├── id: BatchId
  ├── name: BatchName
  ├── batchOf: ProductIdentifier   // do jakiego ProductType należy
  ├── quantityInBatch: Quantity
  ├── dateProduced: Instant?
  ├── sellBy: Instant?
  ├── useBy: Instant?
  ├── bestBefore: Instant?
  ├── startSerialNumber: SerialNumber?
  ├── endSerialNumber: SerialNumber?
  └── comments: String?
```

---

### Warstwa 3: Lot (przedmiot aukcji — "co wystawiamy na sprzedaż")

Kluczowa zmiana: `Lot` przestaje być prostym wrapperem na `Product`. Staje się mostem między inwentarzem a aukcją.

```
Lot (aggregate root)
  ├── id: LotId
  ├── title: String
  ├── description: String
  ├── inventoryEntry: InventoryEntryId    // co fizycznie sprzedajemy
  ├── sellingMode: SellingMode
  ├── minimumBidIncrement: Price
  ├── reservePrice: Price?
  └── status: LotStatus

enum SellingMode {
    WHOLE,           // sprzedaż w całości (samochód, kontener)
    DIVISIBLE,       // sprzedaż w częściach (worki po 100 kg)
    DIVISIBLE_ONLY   // wyłącznie w częściach
}

enum LotStatus {
    DRAFT,
    PUBLISHED,
    ACTIVE,
    SOLD,
    WITHDRAWN,
    EXPIRED
}
```

#### Scenariusze użycia Lot

| Scenariusz | ProductType | TrackingStrategy | SellingMode | Przykład |
|---|---|---|---|---|
| Samochód Jana Kowalskiego | `ProductType.unique()` | UNIQUE | WHOLE | 1 instancja, 1 kupujący |
| Kontener ryżu — całość | `ProductType.batchTracked()` | BATCH_TRACKED | WHOLE | 1 Batch, 1 kupujący |
| Kontener ryżu — worki | `ProductType.batchTracked()` | BATCH_TRACKED | DIVISIBLE | 1 Batch, wiele instancji po 100 kg, wielu kupujących |
| iPhone z pudełkiem | `PackageType` | INDIVIDUALLY_TRACKED | WHOLE | Laptop + torba + myszka |

---

### Warstwa 4: Auction (licytacja — "proces sprzedaży")

```
Auction (aggregate root — już istnieje, rozszerzyć)
  ├── id: AuctionId
  ├── sellerId: PartyId
  ├── lot: LotId                    // ZMIANA: referencja do Lot, nie inline
  ├── startingPrice: Price
  ├── endsAt: Instant
  ├── status: AuctionStatus
  ├── biddingHistory: List<Bid>
  └── winnerAllocation: WinnerAllocation?  // NOWE: kto co wygrał

WinnerAllocation (dla trybu DIVISIBLE)
  ├── allocations: List<Allocation>

Allocation
  ├── winnerId: PartyId
  ├── instanceId: InstanceId        // które instancje wygrał
  ├── quantity: Quantity            // ile jednostek
  └── winningPrice: Price
```

---

### Warstwa 5: Party (podmioty — "kto uczestniczy")

Obecny model jest dobrym startem, ale wymaga rozszerzenia:

```
Party (aggregate root — już istnieje)
  ├── partyId: PartyId
  ├── displayName: String
  └── roles: Set<PartyRole>

// Rozszerzyć o:
PartyRole (sealed interface)
  ├── Seller
  ├── Buyer
  ├── Admin
  └── CustomRole(name: String, validFrom: Instant, validTo: Instant?)

// Relacje między Party (z archetypu Party):
PartyRelationship
  ├── from: PartyId
  ├── to: PartyId
  ├── type: PartyRelationshipType  // otwarty katalog, nie enum
  └── metadata: Map<String, String>
```

---

## Mapa zależności modułów

```
                    ┌──────────┐
                    │  Common  │
                    └────┬─────┘
                         │
          ┌──────────────┼──────────────┐
          │              │              │
    ┌─────▼─────┐  ┌─────▼─────┐  ┌─────▼─────┐
    │  Quantity  │  │   Party   │  │  Pricing  │
    └─────┬─────┘  └─────┬─────┘  └─────┬─────┘
          │              │              │
    ┌─────▼──────────────▼──────────────▼─────┐
    │                Product                   │
    │  (ProductType, PackageType, CatalogEntry)│
    └─────────────────────┬───────────────────┘
                          │
    ┌─────────────────────▼───────────────────┐
    │              Inventory                   │
    │  (InventoryEntry, Instance, Batch)       │
    └─────────────────────┬───────────────────┘
                          │
    ┌─────────────────────▼───────────────────┐
    │                  Lot                     │
    │  (Lot, SellingMode, LotStatus)           │
    └─────────────────────┬───────────────────┘
                          │
    ┌─────────────────────▼───────────────────┐
    │               Auctioning                 │
    │  (Auction, Bid, PlaceBidDecision)        │
    └─────────────────────────────────────────┘
```

---

## Fazy implementacji

### Faza 0: Przygotowanie

1. Dodać moduł `quantity` (Unit, Quantity) — bazowy typ dla wszystkich pomiarów
2. Dodać wspólne typy z `common` (Preconditions, Result, events)

### Faza 1: Product (fundament)

1. `Product` (interfejs composite)
2. `ProductType` — z `ProductTrackingStrategy`, `ProductFeatureTypes`, `ApplicabilityConstraint`
3. `ProductIdentifier` — z wariantami (UUID, GTIN, ISBN)
4. `ProductName`, `ProductDescription`, `ProductMetadata`
5. `ProductFeatureType`, `ProductFeatureInstance`, `FeatureValueConstraint`
6. `PackageType`, `PackageStructure`, `ProductSet`, `SelectionRule`
7. `CatalogEntry`, `Validity`
8. `ProductRelationship`, `ProductRelationshipType`
9. `ProductFacade` — orkiestracja tworzenia/edycji
10. `ProductCatalog` — wyszukiwanie i filtrowanie

### Faza 2: Inventory (inwentarz)

1. `InventoryEntry` — agregat łączący produkt z instancjami
2. `Instance` (interfejs)
3. `ProductInstance` — z SerialNumber, BatchId, Quantity
4. `PackageInstance` — instancja pakietu z wybranymi komponentami
5. `Batch`, `BatchId`, `BatchName` — partia produkcyjna
6. `SerialNumber` — z wariantami (VIN, IMEI, tekstowy)
7. `InventoryFacade` — zarządzanie stanem
8. `AvailabilityFacade` — rezerwacje i blokady

### Faza 3: Lot (przedmiot aukcji)

1. `Lot` — agregat z referencją do `InventoryEntry`
2. `SellingMode` — WHOLE, DIVISIBLE, DIVISIBLE_ONLY
3. `LotStatus` — DRAFT, PUBLISHED, ACTIVE, SOLD, WITHDRAWN, EXPIRED
4. `LotFacade` — tworzenie i publikacja lotów

### Faza 4: Auctioning (licytacje)

1. Refaktoryzacja `Auction` — referencja do `LotId` zamiast inline `Lot`
2. `WinnerAllocation` — dla trybu DIVISIBLE
3. Obsługa wielu zwycięzców w jednej aukcji
4. Eventy domenowe — `AuctionWonEvent`, `AuctionClosedWithoutWinnerEvent`, `AuctionLeaderChangedEvent`

### Faza 5: Party (podmioty)

1. Rozszerzenie `PartyRole` o sealed interface z metadanymi
2. `PartyRelationship` — relacje między podmiotami
3. `PartyFacade` — zarządzanie podmiotami

### Faza 6: Integracje i API

1. OpenAPI — kontrakty dla frontendu
2. Read-side — widoki zdenormalizowane
3. WebSocket — komunikacja w czasie rzeczywistym
4. Integracja z Pricing (cena wywoławcza, rezerwowa)

---

## Kluczowe decyzje architektoniczne

### 1. Composite pattern dla Product

`Product` jako interfejs, `ProductType` i `PackageType` jako implementacje. Pozwala to na:
- Dowolne zagnieżdżanie pakietów
- Spójne API dla obu typów
- Łatwe rozszerzanie o nowe typy (SubscriptionProduct, BundleProduct)

### 2. Eventual consistency dla relacji produktów

Relacje (`ProductRelationship`) są metadanymi biznesowymi, nie twardymi niezmiennikami. Asynchroniczna propagacja zmian jest akceptowalna — chwilowa niespójność rekomendacji nie blokuje sprzedaży.

### 3. Separacja definicji od oferty

`ProductType` mówi, czym coś jest. `CatalogEntry` mówi, że to sprzedajemy. To pozwala:
- Mieć ten sam produkt w wielu kampaniach
- Zmienić ofertę bez zmiany definicji produktu
- Zarządzać dostępnością w czasie

### 4. Inventory jako osobny kontekst

Inwentarz nie jest częścią definicji produktu. To osobny kontekst, który:
- Zarządza fizycznymi egzemplarzami
- Obsługuje dostępność i rezerwacje
- Mapuje instancje na zasoby (availability)

### 5. Lot jako most między Inventory a Auctioning

`Lot` nie jest ani produktem, ani aukcją. Jest decyzją biznesową: "wystawiamy te konkretne egzemplarze z inwentarza na sprzedaż w tym trybie".

---

## Przykłady użycia

### Scenariusz 1: Samochód Jana Kowalskiego

```java
// 1. Definicja produktu
ProductType carType = ProductType.unique(
    ProductIdentifier.uuid(UUID.randomUUID()),
    new ProductName("Samochód osobowy"),
    new ProductDescription("Używany samochód osobowy")
);

// 2. Katalog
CatalogEntry entry = CatalogEntry.builder()
    .displayName("Seat Leon 1999 TDI — Jan Kowalski")
    .description("Zadbany Seat Leon, rocznik 1999, silnik TDI...")
    .product(carType)
    .category("motoryzacja")
    .validity(Validity.from(LocalDate.now()).to(LocalDate.now().plusMonths(1)))
    .build();

// 3. Inwentarz — jeden egzemplarz
Instance instance = InstanceBuilder.forProduct(carType)
    .serialNumber(new VinSerialNumber("WVWZZZ3CZWE123456"))
    .feature(new ProductFeatureInstance(colorType, "czerwony"))
    .feature(new ProductFeatureInstance(yearType, 1999))
    .feature(new ProductFeatureInstance(mileageType, 185000))
    .build();

InventoryEntry inventory = InventoryEntry.create(
    new InventoryProduct(carType.id()),
    availabilityFacade
);
inventory.addInstance(instance);

// 4. Lot — wystawienie na sprzedaż
Lot lot = Lot.create(
    "Seat Leon 1999 TDI — Jan Kowalski",
    inventory.id(),
    SellingMode.WHOLE,
    new Price(BigDecimal.valueOf(5000), "PLN")
);

// 5. Aukcja
Auction auction = Auction.create(
    AuctionId.newId(),
    sellerId,
    lot.id(),
    new Price(BigDecimal.valueOf(5000), "PLN"),
    Instant.now().plusDays(7)
);
```

### Scenariusz 2: Kontener ryżu — sprzedaż podzielna

```java
// 1. Definicja produktu
ProductType riceType = ProductType.batchTracked(
    ProductIdentifier.uuid(UUID.randomUUID()),
    new ProductName("Ryż jaśminowy premium"),
    new ProductDescription("Ryż jaśminowy najwyższej jakości"),
    Unit.kilograms()
);

// 2. Partia produkcyjna
Batch batch = Batch.builder()
    .id(BatchId.random())
    .name(new BatchName("TH-2024-JASMINE-001"))
    .batchOf(riceType)
    .quantityInBatch(new Quantity(2000, Unit.kilograms()))
    .dateProduced(Instant.parse("2024-06-15T00:00:00Z"))
    .bestBefore(Instant.parse("2025-06-15T00:00:00Z"))
    .build();

// 3. Inwentarz — 20 worków po 100 kg
InventoryEntry inventory = InventoryEntry.create(
    new InventoryProduct(riceType.id()),
    availabilityFacade
);

for (int i = 1; i <= 20; i++) {
    Instance bag = InstanceBuilder.forProduct(riceType)
        .batchId(batch.id())
        .quantity(new Quantity(100, Unit.kilograms()))
        .build();
    inventory.addInstance(bag);
}

// 4. Lot — sprzedaż podzielna
Lot lot = Lot.create(
    "Ryż jaśminowy — partia TH-2024-JASMINE-001 (20 worków po 100 kg)",
    inventory.id(),
    SellingMode.DIVISIBLE,
    new Price(BigDecimal.valueOf(200), "PLN")  // cena za worek
);

// 5. Aukcja — wielu zwycięzców możliwe
Auction auction = Auction.create(
    AuctionId.newId(),
    sellerId,
    lot.id(),
    new Price(BigDecimal.valueOf(200), "PLN"),
    Instant.now().plusDays(3)
);
// ... wiele licytacji od różnych kupujących
// ... po zamknięciu: WinnerAllocation z wieloma Allocation
```

### Scenariusz 3: Pakiet — laptop z akcesoriami

```java
// 1. Definicje komponentów
ProductType laptopType = ProductType.individuallyTracked(
    ProductIdentifier.uuid(UUID.randomUUID()),
    new ProductName("Laptop gamingowy"),
    new ProductDescription("Laptop gamingowy z dedykowaną kartą graficzną"),
    Unit.pieces()
);

ProductType bagType = ProductType.identical(
    ProductIdentifier.uuid(UUID.randomUUID()),
    new ProductName("Torba na laptopa"),
    new ProductDescription("Uniwersalna torba na laptopa 15-17\""),
    Unit.pieces()
);

ProductType mouseType = ProductType.identical(
    ProductIdentifier.uuid(UUID.randomUUID()),
    new ProductName("Myszka bezprzewodowa"),
    new ProductDescription("Ergonomiczna myszka bezprzewodowa"),
    Unit.pieces()
);

// 2. Pakiet
ProductSet laptopOptions = ProductSet.singleOf("Laptop", laptopType.id());
ProductSet bagOptions = ProductSet.of("Torba", bagType.id());
ProductSet mouseOptions = ProductSet.of("Myszka", mouseType.id());

PackageStructure structure = new PackageStructure(
    List.of(laptopOptions, bagOptions, mouseOptions),
    List.of(
        SelectionRule.single(laptopOptions),    // dokładnie 1 laptop
        SelectionRule.optional(bagOptions),     // opcjonalnie torba
        SelectionRule.optional(mouseOptions)    // opcjonalnie myszka
    )
);

PackageType laptopBundle = PackageType.define(
    ProductIdentifier.uuid(UUID.randomUUID()),
    new ProductName("Pakiet gamingowy"),
    new ProductDescription("Laptop gamingowy z akcesoriami"),
    structure
);

// 3. Instancja pakietu — klient wybiera konfigurację
PackageInstance bundleInstance = PackageInstance.builder()
    .packageType(laptopBundle)
    .select(laptopType.id(), 1)
    .select(bagType.id(), 1)
    .build();

// 4. Inwentarz i Lot
// ... analogicznie jak wyżej
```

---

## Mapa konwersji: obecny model → nowy model

| Obecny element | Nowy element | Uwagi |
|---|---|---|
| `Product` | `ProductType` | Rozbudować o tracking, cechy, metadane |
| `Lot.product` | `Lot.inventoryEntry` + `Lot.sellingMode` | Lot referuje InventoryEntry, nie Product |
| `Auction.lot` | `Auction.lotId` | Referencja przez ID, nie inline |
| — | `CatalogEntry` | Nowy byt — oferta handlowa |
| — | `InventoryEntry` | Nowy agregat — fizyczne egzemplarze |
| — | `Instance` / `ProductInstance` | Nowy byt — konkretne egzemplarze |
| — | `Batch` | Nowy byt — partie produkcyjne |
| — | `PackageType` / `PackageInstance` | Nowy byt — pakiety |
| `Party` | `Party` + `PartyRelationship` | Rozbudować o relacje |
| `Bid` | `Bid` + `WinnerAllocation` | Rozbudować o alokację dla DIVISIBLE |

---

## Ryzyka i mitigacje

| Ryzyko | Mitigacja |
|---|---|
| Zbyt duży zakres — próbujemy zrobić wszystko naraz | Fazy 0-6, każda z deliverable |
| Breaking changes w API | Nowe endpointy obok starych, stopniowa migracja |
| Złożoność PackageType na start | Zacząć od ProductType, PackageType w późniejszej fazie |
| Brak doświadczenia z DDD / archetypami | Kod referencyjny z archetypes repo + PDF/TXT jako dokumentacja |

---

## Następne kroki

1. **Faza 0** — dodać moduł `quantity` (Unit, Quantity)
2. **Faza 1** — zacząć od `ProductType` z `ProductTrackingStrategy`
3. **Faza 2** — `InventoryEntry` i `Instance`
4. **Faza 3** — `Lot` z `SellingMode`
5. **Faza 4** — refaktoryzacja `Auction`

Każda faza powinna mieć:
- Modele domenowe
- Testy jednostkowe
- Facade (API wewnątrzkontekstowe)
- Repository (interfejs + in-memory implementacja)
