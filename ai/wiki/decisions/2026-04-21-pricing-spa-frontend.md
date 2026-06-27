# ADR: Pricing SPA Frontend Module

**Data**: 2026-04-21
**Status**: Accepted
**Tagi**: #pricing #spa #angular #frontend

## TLDR

Dodano moduł pricing do Angular SPA, który integruje się z backendowymi endpointami `/api/auctions/{id}/price`, `/api/auctions/{id}/wadium` i `/api/auctions/{id}/wadium/refund`. Frontend pokazuje kalkulację ceny, pozwala na wpłatę/zwrot wadium i konfiguruje parametry cenowe przy tworzeniu aukcji.

## Kontekst

Backendowy moduł `hexabid-pricing` (opisany w [[decisions/2026-04-17-pricing-architecture]]) oferuje:
- `GET /api/auctions/{id}/price` - kalkulacja ceny (hammerPrice, wadiumOffset, netto, excise, customsDuty, vat, totalDue)
- `POST /api/auctions/{id}/wadium` - wpłata wadium
- `POST /api/auctions/{id}/wadium/refund` - zwrot wadium

OpenAPI contract generuje TypeScript types: `AuctionPriceBreakdownResponse`, `WadiumResponse`, `WadiumRefundResponse`, `PricingConfig`, `Money`, `AppliedRates`.

Frontend nie miał żadnej integracji z tymi endpointami.

## Decyzja

### Warstwa data-access

Nowe pliki w `hexabid-spa/src/app/data-access/`:

1. **`contracts/pricing-api.models.ts`** - View models (VM) dla pricing:
   - `PriceBreakdownVm` - pełny rozkład ceny z etykietami PL
   - `WadiumDepositVm` / `WadiumRefundVm` - wynik wpłaty/zwrotu z polskimi statusami
   - `PricingConfigVm` - konfiguracja cenowa aukcji
   - `MoneyVm` - kwota z etykietą (np. "500.00 PLN")
   - `AppliedRatesVm` - zastosowane stawki z etykietami

2. **`mappers/pricing-view.mapper.ts`** - Mapowanie z generated types na VM:
   - `toPriceBreakdownVm()` - generuje polskie etykiety (wadiumType: "Stałe"/"Procentowe", statusLabel: "Wpłacone"/"Zwrócone")
   - `toWadiumDepositVm()`, `toWadiumRefundVm()`, `toPricingConfigVm()`

3. **`http/pricing-api.service.ts`** - Serwis API:
   - `getPriceBreakdown(auctionId)` - GET price
   - `depositWadium(auctionId, amount, currency)` - POST wadium
   - `refundWadium(auctionId, partyId)` - POST refund
   - Obsługa błędów 401 (wymagane logowanie) i problem details

### Warstwa feature

Nowy feature module w `hexabid-spa/src/app/features/pricing/`:

1. **`pricing.facade.ts`** - `PricingFacade` (scoped provider):
   - Sygnały: `loading`, `error`, `breakdown`, `wadiumDepositing`, `wadiumRefunding`, `wadiumDeposit`, `wadiumRefund`
   - Metody: `loadBreakdown()`, `depositWadium()`, `refundWadium()`
   - Po wpłacie/zwrocie wadium automatycznie odświeża kalkulację

2. **`pricing-page.component.ts/html/scss`** - `PricingPageComponent`:
   - Route: `/auction/:auctionId/pricing`
   - Wyświetla tabelę kalkulacji: hammerPrice, wadiumOffset, netto, excise, customsDuty, vat, totalDue
   - Panel "Stawki zastosowane" z opisem stawek
   - Formuła kalkulacji (edukacyjna)
   - Formularz wpłaty wadium (amount + currency)
   - Formularz zwrotu wadium (partyId)

### Rozszerzenia istniejących stron

1. **Auction Details** (`details/auction-details-page.component`):
   - Dodano link "Zobacz kalkulację ceny →" prowadzący do `/auction/{id}/pricing`
   - Dodano `RouterLink` do imports

2. **Auction Create** (`create/auction-create-page.component`):
   - Przycisk "Dodaj konfigurację ceny" rozwijający sekcję PricingConfig
   - Dynamiczne formularze: stawka wadium (procentowe/stałe), VAT, akcyza (typ PERCENTAGE/PER_UNIT), cło
   - Warunkowe wyświetlanie pól zależne od wybranych opcji
   - Bezpośrednie użycie typów `CreateAuctionRequest` i `PricingConfig` (typed, nie `Record<string, unknown>`)

### Ruting

Nowa ruta w `app.routes.ts`:
```
/auction/:auctionId/pricing -> PricingPageComponent (lazy loaded)
```

### E2E Tests

3 nowe pliki Playwright w `hexabid-spa/e2e/`:

1. **`pricing.spec.ts`** (4 testy):
   - Wyświetla stronę kalkulacji (lub błąd API)
   - Link powrotu do aukcji
   - Strona renderuje się bez błędów
   - URL zawiera identyfikator aukcji

2. **`pricing-create.spec.ts`** (4 testy):
   - Rozwinięcie sekcji konfiguracji ceny
   - Wybór strategii wadium (procentowe/stałe)
   - Pola akcyzy i cła
   - Ukrycie sekcji po ponownym kliknięciu

3. **`pricing-navigation.spec.ts`** (3 testy):
   - Strona szczegółów renderuje się
   - Link do kalkulacji jest obecny
   - Nawigacja do strony kalkulacji

## Konsekwencje

### Pozytywne
- Użytkownik widzi pełny rozkład ceny z polskimi etykietami
- Edukacyjna formuła kalkulacji pomaga zrozumieć obliczenia
- Konfiguracja cenowa przy tworzeniu aukcji pozwala na pełne testy E2E
- Wadium jest zarządzane bezpośrednio z UI
- Separacja warstw (data-access → feature) jest zachowana

### Ograniczenia
- Wadium deposit/refund wymaga autentykacji (backend 401)
- Strona kalkulacji pokazuje błąd gdy auctionId nie istnieje
- PricingConfig w formularzu tworzenia jest opcjonalny (toggle)

## Pliki

### Nowe
```
hexabid-spa/src/app/data-access/contracts/pricing-api.models.ts
hexabid-spa/src/app/data-access/mappers/pricing-view.mapper.ts
hexabid-spa/src/app/data-access/http/pricing-api.service.ts
hexabid-spa/src/app/features/pricing/pricing.facade.ts
hexabid-spa/src/app/features/pricing/pricing-page.component.ts
hexabid-spa/src/app/features/pricing/pricing-page.component.html
hexabid-spa/src/app/features/pricing/pricing-page.component.scss
hexabid-spa/e2e/pricing.spec.ts
hexabid-spa/e2e/pricing-create.spec.ts
hexabid-spa/e2e/pricing-navigation.spec.ts
```

### Zmodyfikowane
```
hexabid-spa/src/app/app.routes.ts (nowa ruta /auction/:auctionId/pricing)
hexabid-spa/src/app/features/details/auction-details-page.component.ts (RouterLink import)
hexabid-spa/src/app/features/details/auction-details-page.component.html (pricing link)
hexabid-spa/src/app/features/details/auction-details-page.component.scss (pricing-link style)
hexabid-spa/src/app/features/create/auction-create-page.component.ts (PricingConfig form)
hexabid-spa/src/app/features/create/auction-create-page.component.html (pricing config section)
hexabid-spa/src/app/features/create/auction-create-page.component.scss (pricing section styles)
```

## Powiązane dokumenty

- [[decisions/2026-04-17-pricing-architecture]] - Backendowa architektura pricing
- [[concepts/pricing-spa-layers]] - Szczegóły warstw frontendowych
