# Pricing SPA Layers

**Tagi**: #pricing #spa #angular #frontend

## TLDR

Frontendowy moduł pricing w Hexabid SPA składa się z trzech warstw: data-access (kontrakty, mappery, serwis HTTP), feature (facade, komponent strony) i e2e (Playwright). Każda warstwa ma jasną odpowiedzialność.

## Warstwy

### Data-Access

Zgodnie z konwencją Hexabid SPA, wygenerowany kod OpenAPI (`generated/auction-contract`) jest NIGDY nieedytowany ręcznie. Nad nim budowane są:

1. **Kontrakty** (`data-access/contracts/pricing-api.models.ts`):
   - View models (VM) z polskimi etykietami
   - Rozszerzają wygenerowane types o label-e, statusLabel-e, formatLabel-e
   - Przykład: `MoneyVm` = `{ amount, currency, label }` (label = "500.00 PLN")

2. **Mappery** (`data-access/mappers/pricing-view.mapper.ts`):
   - Funkcje czyste: `toPriceBreakdownVm()`, `toWadiumDepositVm()`, `toWadiumRefundVm()`, `toPricingConfigVm()`
   - Etykiety PL: wadiumType → "Stałe"/"Procentowe", status → "Wpłacone"/"Zwrócone"/"Potrącone"
   - Null-safety: opcjonalne pola backendu mapowane na `null` (nie `undefined`)

3. **Serwis HTTP** (`data-access/http/pricing-api.service.ts`):
   - Wrapuje wygenerowany `AuctionsApi` client
   - Metody: `getPriceBreakdown()`, `depositWadium()`, `refundWadium()`
   - Normalizacja błędów: 401 → "Ta operacja wymaga zalogowania", problem details extraction
   - `providedIn: 'root'` (singleton)

### Feature

Feature module (`features/pricing/`):

1. **Facade** (`pricing.facade.ts`):
   - Angular `Injectable()` scoped do komponentu (nie `providedIn: 'root'`)
   - Sygnały Angular 20: `loading`, `error`, `breakdown`, `wadiumDepositing`, `wadiumRefunding`
   - Automatyczne odświeżanie kalkulacji po wpłacie/zwrocie wadium
   - Zależności: `PricingApiService`

2. **Komponent strony** (`pricing-page.component.ts/html/scss`):
   - Route: `/auction/:auctionId/pricing`
   - Lazy loaded
   - `ChangeDetectionStrategy.OnPush`
   - Dwie sekcje formularzy: wpłata wadium, zwrot wadium
   - Tabela kalkulacji z warunkowym wyświetlaniem (akcyza/cło ukryte gdy 0.00)
   - Panel edukacyjny z formułą kalkulacji

### E2E

Playwright tests (`e2e/`):

- Testy UI-only (nie wymagają backendu do testowania renderowania)
- Testy sprawdzają: nawigację, widoczność elementów, dynamiczne formularze
- Testy kalkulacji degradują gracefully (error state gdy API niedostępne)

## Konwencje

- **Polskie etykiety** w VM (nie w wygenerowanym kodzie)
- **Pola `Label`** w VM: `statusLabel`, `wadiumTypeLabel`, `priceLabel`
- **Null zamiast undefined** w VM
- **Facade per page** (scoped, nie singleton)
- **Formularze reactive** z `FormControl` i `FormGroup`
