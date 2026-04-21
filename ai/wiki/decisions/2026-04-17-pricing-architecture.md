# ADR: Architektura Ceny w Aukcjach (Pricing Architecture)

## TLDR

Wprowadzenie modułu `hexabid-pricing` opartego na archetypie Pricing (M03) i Rules (M04), modelującego cenę jako kompozycję składowych (hammer price, wadium, akcyza, cło, VAT, prowizje) zamiast pojedynczej liczby. Cena staje się funkcją kontekstu aukcji, a jej składowe są jawne i audytowalne.

## Tagi

#pricing #wadium #vat #akcyza #clo #archetype-pricing #archetype-rules #component-tree #calculator

## Kontekst

### Problem

Obecny model `Price(BigDecimal amount, String currency)` to antywzorzec zidentyfikowany w archetypie Pricing (M03L01): "Cena to nie liczba. Cena to funkcja. Cena to decyzja." Brak możliwości:

1. **Wadium** - wpłata przed licytacją, obniżająca cenę po wygranej
2. **VAT** - komponent zależny od kwoty netto, różne stawki wg typu produktu
3. **Akcyza** - podatek akcyzowy dla towarów akcyzowych (alkohol, paliwa, samochody, tytoń); obliczany przed VAT
4. **Cło** - opłata celna dla towarów importowanych; zależna od kodu taryfy celnej i kraju pochodzenia
5. **Rozbicie ceny** - system widzi jedną liczbę, a biznes potrzebuje tortu (netto, akcyza, cło, VAT, wadium, prowizje)
6. **Typy produktów** - UNIQUE (samochód Kowalskiego) vs IDENTICAL/BATCH (100kg mąki z kontenera) wpływają na sposób liczenia ceny

### Odnośniki do wiedzy z archetypów

| Archetyp | Lekcja | Kluczowa koncepcja | Zastosowanie w Hexabid |
|----------|--------|--------------------|-----------------------|
| M03 Pricing | M03L01 | Cena to nie liczba, to funkcja | Price -> PricingComponent |
| M03 Pricing | M03L03 | Calculator jako abstrakcja | PriceCalculator + implementacje |
| M03 Pricing | M03L03 | CompositeFunctionCalculator | Taryfy dla produktów podzielnych |
| M03 Pricing | M03L04 | Interpretation: TOTAL, UNIT, MARGINAL | Cena za sztukę vs całościowa |
| M03 Pricing | M03L05 | Component - warstwa semantyczna | AuctionPriceComponent z nazwą biznesową |
| M03 Pricing | M03L05 | CompositeComponent + zależności | VAT zależy od netto+akcyza+cło, wadium obniża netto |
| M03 Pricing | M03L05 | ComponentBreakdown | AuctionPriceBreakdown |
| M03 Pricing | M03_ZP | calculator+validity+applicability+component | Struktura modułu pricing |
| M04 Rules | M04L01 | Ograniczenia if-ologii | Unikanie if-ów dla stawek VAT/akcyza/cło/wadium |
| M04 Rules | M04L02 | Trójpodział: core, domknięcia, fabryka | Core aukcji vs polityki wadium/VAT/akcyza/cło |
| M04 Rules | M04L02 | ConfigurableModifier: predicate+applier+guardian | Modyfikator ceny z warunkiem + strażnikiem |
| M04 Rules | M04L03 | Reguły jako uniwersalny mechanizm | Silnik reguł dla polityk cenowych |
| M04 Rules | M04L04 | Integracja reguł z architekturą | AuctionWonEvent -> PricingFacade |

## Decyzja

### Nowy moduł domenowy: hexabid-pricing

Moduł czystej Javy (bez Spring), implementujący archetyp Pricing wg wzorca z M03:

```
hexabid-pricing/
  src/main/java/com/github/hexabid/pricing/
    calculator/                          # WARSTWA CAPABILITY (M03L03, M03L05)
      PriceCalculator.java               # interfejs: calculate(PricingContext) -> Money
      CalculatorType.java                # SIMPLE_FIXED, PERCENTAGE, PER_UNIT, TIERED_MARGINAL
      SimpleFixedCalculator.java         # stała kwota (np. opłata za sesję)
      PercentageCalculator.java          # procent od bazy (np. VAT 23%, wadium 10%)
      PerUnitCalculator.java             # stawka za jednostkę (np. 5 PLN/kg)
      TieredMarginalCalculator.java      # schodkowy cennik marginalny (np. progi ilościowe)

    component/                           # WARSTWA SEMANTYCZNA (M03L05)
      PricingComponent.java             # interfejs: calculate(PricingContext) -> ComponentResult
      SimplePriceComponent.java          # liść: nazwa biznesowa + calculator + mapping parametrów
      CompositePriceComponent.java       # kompozyt: dzieci + zależności między nimi
      ComponentResult.java              # wartość + nazwa + breakdown dzieci
      ParameterMapping.java             # mapowanie nazw biznesowych na parametry calculatora
      ParameterDependencies.java         # zależności: wynik komponentu X jako parametr Y

    model/                              # VALUE OBJECTS
      Money.java                         # kwota + waluta + arytmetyka (zastąpi Price z core)
      Interpretation.java               # TOTAL, UNIT, MARGINAL (M03L04)
      VatRate.java                       # stawka VAT (23%, 8%, 5%, 0%, zwolniony)
      ExciseRate.java                    # stawka akcyzy (kwotowa: PLN/litr, PLN/sztuka; lub procentowa)
      CustomsDutyRate.java               # stawka cła (procentowa wg kodu HS + kraj pochodzenia)
      Wadium.java                        # wartość wadium + strategia (FIXED / PERCENTAGE)
      WadiumStrategy.java               # interfejs: calculateWadium(startingPrice) -> Money
      FixedWadium.java                  # stała kwota wadium
      PercentageWadium.java             # procent od ceny wywoławczej
      TaxDeterminationPolicy.java       # interfejs: wybór stawek VAT/akcyza/cło wg kontekstu
      PricingContext.java               # kontekst: hammerPrice, quantity, productType, originCountry, hsCode, itd.

    auction/                            # ORKIESTRACJA (WARSTWA OPERATIONS)
      AuctionPrice.java                  # fasada budująca drzewo komponentów dla aukcji
      AuctionPriceBreakdown.java         # wynik: hammerPrice, wadium, netto, akcyza, cło, vat, totalDue
      AuctionPricingFacade.java          # punkt wejścia: calculateAuctionPrice() -> Breakdown
```

### Architektura trójwarstwowa (z M04L02)

Zgodnie z archetypem Rules (M04L02), logika dzieli się na trzy strefy podatności na zmiany:

**1. Stabilny core (Operations)**
- `AuctionPricingFacade` - procedura obliczania ceny aukcji
- Przepływ: hammer price -> odjęcie wadium -> akcyza (na netto po wadium) -> cło (na netto+wadium+akcyza) -> VAT (na netto+akcyza+cło) -> suma = do zapłaty
- Rzadko ulega zmianom - to szkielet procesu cenowego

**2. Domknięcia (Policy)**
- `WadiumStrategy` - jak liczyć wadium (stałe vs procentowe)
- `VatRate` - która stawka VAT ma zastosowanie
- `ExciseRate` - czy produkt podlega akcyzie i jaka stawka
- `CustomsDutyRate` - czy produkt importowany i jaka stawka cła
- `PriceCalculator` implementacje - jak liczyć poszczególne składniki
- Często zmieniane przez biznes bez ingerencji w core

**3. Fabryka (Selection)**
- Logika wyboru: która strategia wadium, która stawka VAT, czy akcyza ma zastosowanie, czy cło ma zastosowanie
- Na podstawie kontekstu: typ produktu, kategoria, kod HS, kraj pochodzenia, jurysdykcja podatkowa
- Zmiany w wyborze polityk nie wpływają na core ani na same polityki

### Drzewo komponentów ceny aukcyjnej (M03L05)

Kluczowe: kalkulatory to matematyka (capability), komponenty to semantyka (operations). Jeden PercentageCalculator obsługuje VAT, akcyzę i cło — różni je tylko nazwa biznesowa i parametry.

```
auction-total (CompositePriceComponent)
├── netto (CompositePriceComponent)
│   ├── hammer-price       (SimplePriceComponent, calculator=SimpleFixed, interpretation=TOTAL)
│   └── wadium-offset      (SimplePriceComponent, calculator=PercentageCalculator lub SimpleFixed, interpretation=TOTAL)
│       └── zależność: wadium-offset.oblicz_od = hammer-price
├── excise                 (SimplePriceComponent, calculator=ExciseCalculator lub PercentageCalculator)
│   └── zależność: excise.base_amount = netto  [tylko gdy produkt akcyzowy]
├── customs-duty           (SimplePriceComponent, calculator=PercentageCalculator)
│   └── zależność: customs-duty.base_amount = netto + excise  [tylko gdy produkt importowany]
└── vat                    (SimplePriceComponent, calculator=PercentageCalculator)
    └── zależność: vat.base_amount = netto + excise + customs-duty
```

**Formuła końcowa:**

```
netto       = hammer_price - wadium_offset   (wadium obniża podstawę opodatkowania)
excise      = f(netto)                       (akcyza - gdy ma zastosowanie)
customs     = f(netto + excise)               (cło - gdy produkt importowany)
vat         = (netto + excise + customs) * vat_rate
totalDue    = netto + excise + customs + vat
```

### Wpływ typu produktu na drzewo komponentów (M03L03, M03L04)

| Cecha produktu | Wpływ na drzewo komponentów | Przykład |
|----------------|-----------------------------|----------|
| UNIQUE (samochód) | Brak komponentu per-unit; Interpretation=TOTAL | Jan Kowalski's car za 50 000 PLN |
| IDENTICAL/BATCH (mąka) | PerUnitCalculator dla ceny za kg; Interpretation=UNIT lub MARGINAL | 100 kg mąki po 3 PLN/kg |
| Produkt akcyzowy | Komponent excise aktywny | Alkohol, paliwo, samochód |
| Produkt importowany | Komponent customs-duty aktywny | Elektronika z USA |
| Podzielny (DIVISIBLE) | TieredMarginalCalculator; różne ceny za progi ilościowe | 1-10 kg: 5 PLN/kg, 10+ kg: 4 PLN/kg |
| Niepodzielny (WHOLE) | SimpleFixedCalculator; jedna cena za całość | Obraz, antyk |

### Applicability (M03_ZP) - warunkowe aktywowanie komponentów

Zgodnie z Zadaniem Praktycznym M03_ZP, każdy komponent ma regułę zastosowania (applicability):

- **excise-component**: aktywny gdy `productType.isExcisable() == true`
- **customs-duty-component**: aktywny gdy `productOrigin.isImported() == true`
- **wadium-offset**: aktywny gdy `auction.hasWadiumPaid() == true`
- **vat-component**: aktywny zawsze (ale stawka zależy od `productType.getVatRate()`)

### Validity (M03_ZP) - wersjonowanie w czasie

Stawki VAT, akcyzy i cła zmieniają się w czasie. Model wspiera DateRange:

- `VatRate.validFrom / validTo` - która stawka obowiązuje w danym okresie
- `ExciseRate.validFrom / validTo` - zmiany stawek akcyzowych
- `CustomsDutyRate.validFrom / validTo` - zmiany taryf celnych

Cena wyliczana jest na podstawie stawek obowiązujących w momencie zamknięcia aukcji, nie na podstawie stawek bieżących (M03L02: "historia instrukcji, nie tylko historia liczb").

## Scenariusze E2E

### E2E-1: Aukcja samochodu (UNIQUE, akcyza, brak cła)

**Produkt:** Samochód Jana Kowalskiego (UNIQUE, produkt akcyzowy, produkcja krajowa)

**Dane wejściowe:**
- Hammer price: 50 000 PLN
- Wadium wpłacone: 5% od ceny wywoławczej (40 000 PLN) = 2 000 PLN
- Akcyza: 3.1% dla silników 2.0L (krajefikacja PL)
- VAT: 23%
- Cło: brak (produkcja krajowa)

**Obliczenia:**
```
netto     = 50 000 - 2 000 = 48 000 PLN
excise    = 48 000 * 3.1% = 1 488 PLN
customs   = 0 PLN (brak - krajowy)
vat       = (48 000 + 1 488 + 0) * 23% = 11 382.24 PLN
totalDue  = 48 000 + 1 488 + 0 + 11 382.24 = 60 870.24 PLN
```

**Asercje:**
- AuctionPriceBreakdown.hammerPrice() == 50 000 PLN
- AuctionPriceBreakdown.wadiumOffset() == 2 000 PLN
- AuctionPriceBreakdown.netto() == 48 000 PLN
- AuctionPriceBreakdown.excise() == 1 488 PLN
- AuctionPriceBreakdown.customs() == 0 PLN
- AuctionPriceBreakdown.vat() == 11 382.24 PLN
- AuctionPriceBreakdown.totalDue() == 60 870.24 PLN

### E2E-2: Aukcja mąki z importu (BATCH_TRACKED, cło, brak akcyzy)

**Produkt:** 100 kg mąki z kontenera, import z Ukrainy (IDENTICAL/BATCH, nieakcyzowy, importowany)

**Dane wejściowe:**
- Cena za kg: 3 PLN (PerUnitCalculator)
- Ilość: 100 kg
- Wadium: stałe 50 PLN
- Cło: 5% wg kodu HS 1101.00
- VAT: 5% (stawka na podstawowe produkty spożywcze)
- Akcyza: brak

**Obliczenia:**
```
hammerPrice = 100 * 3 = 300 PLN
netto        = 300 - 50 = 250 PLN
excise       = 0 PLN (nieakcyzowy)
customs      = (250 + 0) * 5% = 12.50 PLN
vat          = (250 + 0 + 12.50) * 5% = 13.125 PLN -> 13.13 PLN (zaokrąglenie)
totalDue     = 250 + 0 + 12.50 + 13.13 = 275.63 PLN
```

**Asercje:**
- AuctionPriceBreakdown.hammerPrice() == 300 PLN
- AuctionPriceBreakdown.wadiumOffset() == 50 PLN
- AuctionPriceBreakdown.netto() == 250 PLN
- AuctionPriceBreakdown.excise() == 0 PLN
- AuctionPriceBreakdown.customs() == 12.50 PLN
- AuctionPriceBreakdown.vat() == 13.13 PLN
- AuctionPriceBreakdown.totalDue() == 275.63 PLN

### E2E-3: Aukcja elektroniki importowanej (UNIQUE, akcyza + cło)

**Produkt:** iPhone 15 Pro (INDIVIDUALLY_TRACKED, akcyzowa - akcyza na elektronikę pow. 2000 PLN w pewnych jurysdykcjach, import z USA)

**Dane wejściowe:**
- Hammer price: 6 000 PLN
- Wadium: 10% od ceny wywoławczej (5 000 PLN) = 500 PLN
- Akcyza: 0 PLN (w PL elektronika nie jest akcyzowa - komponent nieaktywny)
- Cło: 0% (smartfony mają stawkę 0% w UE wg HS 8517.12)
- VAT: 23%

**Obliczenia:**
```
netto     = 6 000 - 500 = 5 500 PLN
excise    = 0 PLN (nieakcyzowy w PL)
customs   = 0 PLN (stawka 0% w UE)
vat       = 5 500 * 23% = 1 265 PLN
totalDue  = 5 500 + 0 + 0 + 1 265 = 6 765 PLN
```

### E2E-4: Aukcja alkoholu (BATCH_TRACKED, akcyza kwotowa)

**Produkt:** 50 butelek wina (BATCH_TRACKED, akcyzowy, import z Francji)

**Dane wejściowe:**
- Cena za butelkę: 40 PLN, ilość: 50 szt = 2 000 PLN hammer
- Wadium: 5% od 1 800 PLN = 90 PLN
- Akcyza: kwotowa 1.56 PLN/litr, butelka 0.75L = 1.17 PLN/butelka * 50 = 58.50 PLN
- Cło: 0% (wino z UE)
- VAT: 23%

**Obliczenia:**
```
netto     = 2 000 - 90 = 1 910 PLN
excise    = 58.50 PLN (akcyza kwotowa, PercentageCalculator z CalculatorType=PER_UNIT na litry)
customs   = 0 PLN (w UE)
vat       = (1 910 + 58.50 + 0) * 23% = 452.755 -> 452.76 PLN
totalDue  = 1 910 + 58.50 + 0 + 452.76 = 2 421.26 PLN
```

## Scenariusze po endpointach

### EP-1: POST /api/v1/auctions/{id}/price - Obliczenie ceny aukcyjnej

**Request:**
```json
{
  "hammerPrice": {"amount": "50000.00", "currency": "PLN"},
  "wadiumPaid": {"amount": "2000.00", "currency": "PLN"},
  "productType": "UNIQUE",
  "isExcisable": true,
  "isImported": false,
  "hsCode": null,
  "originCountry": null
}
```

**Response 200:**
```json
{
  "breakdown": {
    "hammerPrice": {"amount": "50000.00", "currency": "PLN"},
    "wadiumOffset": {"amount": "2000.00", "currency": "PLN"},
    "netto": {"amount": "48000.00", "currency": "PLN"},
    "excise": {"amount": "1488.00", "currency": "PLN"},
    "customsDuty": {"amount": "0.00", "currency": "PLN"},
    "vat": {"amount": "11382.24", "currency": "PLN"},
    "totalDue": {"amount": "60870.24", "currency": "PLN"}
  },
  "appliedRates": {
    "vatRate": "23%",
    "exciseRate": "3.1%",
    "customsDutyRate": null,
    "wadiumType": "PERCENTAGE"
  }
}
```

### EP-2: GET /api/v1/auctions/{id}/price - Pobranie rozbicia ceny zamkniętej aukcji

**Response 200:** (jako wyżej + metadane czasowe)
```json
{
  "breakdown": { ... },
  "appliedRates": { ... },
  "calculatedAt": "2026-04-17T14:30:00Z",
  "validAsOf": "2026-04-17T14:30:00Z",
  "rateVersion": {
    "vatRateValidFrom": "2026-01-01",
    "exciseRateValidFrom": "2026-01-01"
  }
}
```

### EP-3: POST /api/v1/auctions - Tworzenie aukcji z konfiguracją cenową

**Request:**
```json
{
  "title": "Samochód Jana Kowalskiego - Volvo XC60",
  "startingPrice": {"amount": "40000.00", "currency": "PLN"},
  "endsAt": "2026-05-01T18:00:00Z",
  "lotId": "lot-123",
  "pricingConfig": {
    "wadiumStrategy": "PERCENTAGE",
    "wadiumRate": "0.05",
    "vatRate": "23%",
    "isExcisable": true,
    "exciseRate": "3.1%",
    "isImported": false,
    "customsDutyRate": null
  }
}
```

### EP-4: POST /api/v1/auctions/{id}/bids - Licytacja (bez zmian - wadium sprawdzone osobno)

PlaceBidCommand pozostaje bez zmian. Wadium jest weryfikowane osobno (port in: VerifyWadiumUseCase) przed dopuszczeniem do licytacji.

**Nowy endpoint wadium:**

### EP-5: POST /api/v1/auctions/{id}/wadium - Wpłata wadium

**Request:**
```json
{
  "amount": {"amount": "2000.00", "currency": "PLN"},
  "paymentMethod": "PAYU"
}
```

**Response 201:**
```json
{
  "wadiumId": "wad-456",
  "status": "PAID",
  "amount": {"amount": "2000.00", "currency": "PLN"},
  "refundableOnLoss": true,
  "deductibleOnWin": true
}
```

### EP-6: GET /api/v1/products/{id}/pricing-rules - Reguły cenowe dla typu produktu

**Response 200:**
```json
{
  "productId": "prod-789",
  "productType": "UNIQUE",
  "trackingStrategy": "INDIVIDUALLY_TRACKED",
  "pricingRules": {
    "isExcisable": true,
    "exciseCalculatorType": "PERCENTAGE",
    "isImportable": false,
    "defaultVatRate": "23%",
    "applicableCustomsDutyRates": [],
    "wadiumRequired": true,
    "defaultWadiumStrategy": "PERCENTAGE",
    "defaultWadiumRate": "0.05"
  }
}
```

## Integracja z istniejącymi modułami

### hexabid-core (Auction, Bid)
- `AuctionWonEvent` zyskuje pole `AuctionPriceBreakdown` zamiast samego `Price`
- `Auction.currentPrice()` zwraca hammer price (bez rozbicia)
- `AuctionPricingFacade` wywoływany przy close -> generuje pełny breakdown

### hexabid-payment-core
- `ProcessPaymentUseCase` odbiera `AuctionPriceBreakdown` zamiast `Price`
- Księgowanie z podziałem na składniki (DEBIT entry per komponent: netto, VAT, akcyza, cło)

### hexabid-product
- `ProductType` zyskuje `isExcisable()`, `getDefaultVatRate()`, `getHsCode()`
- `Batch` zyskuje `getOriginCountry()` dla określenia cła

### hexabid-inventory
- `InventoryEntry` referencjonuje `ProductType` z informacją o reżimie podatkowym

## Konsekwencje

### Plusy
- Pełna transparentność ceny - każdy składnik ma nazwę i wartość
- Zgodność podatkowa - prawidłowa kolejność: akcyza przed VAT, cło przed VAT
- Extensibility - nowe składniki (prowizja, opłata manipulacyjna) to nowy SimplePriceComponent
- Audytowalność - ComponentBreakdown + validity stawek = pełna historia

### Minusy
- Złożoność: zamiast Price(amount, currency) mamy drzewo komponentów
- Migracja: AuctionWonEvent i ProcessPaymentUseCase wymagają zmiany kontraktu
- Krzywa uczenia: zespół musi zrozumieć archetyp Pricing (calculator vs component vs interpretation)

### Ryzyka
- Nadmierne projektowanie dla prostych aukcji bez akcyzy/cła -> mitigacja: applicability włącza/wyłącza komponenty
- Rozbieżność zaokrągleń między calculatorami -> mitigacja: fasada zarządza precyzją, clienty nie liczą samodzielnie (M03L04)
