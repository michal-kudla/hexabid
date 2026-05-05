# E2E scenariusze biznesowe SPA (Playwright)

## Tytuł
Scenariusze click-through GUI dla pełnych flow biznesowych.

## Podsumowanie (TLDR)
- E2E testy odtwarzają realne "przeklikanie" aplikacji po większych zmianach.
- Scenariusze pokrywają przekrojowo: logowanie dev, produkt, inventory, aukcję, reguły i pricing.
- Brak kluczowych danych/ekranów kończy test błędem (bez cichego skipa).

## Tagi
#e2e #playwright #spa #regresja #business-flow

## Treść
### Zakres smoke suite
1. `katalog produktów + filtrowanie` — sanity check modułu Product.
2. `tworzenie partii produktu` — kompletność GUI dla Inventory (formularz, pola, walidacja obecności).
3. `rynek -> szczegóły aukcji -> pricing` — krytyczny flow kupującego.
4. `formularz wystawiania + pricing config` — krytyczny flow sprzedającego.
5. `rules + document submit UI` — widoczność reguł i dokumentów po stronie GUI.
6. `dev auth account picker` — wybór użytkownika dev z GUI kończy się aktywną sesją i profilem `/api/me`.

### Artefakty diagnostyczne
- Na każdej awarii Playwright zapisuje trace/screenshot/video.
- Dodatkowo testy robią screenshoty kroków biznesowych jako attachments.

### Jak uruchamiać lokalnie
1. Backend (profile lokalne + seed data):
   - `mvn -Plocal -DskipTests install`
   - `systemctl --user restart hexabid-backend`
2. Frontend + e2e:
   - `cd hexabid-spa`
   - `npm run e2e:auth`
   - `npm run e2e:business`

### Debug
- Raport HTML: `npm run e2e:report`
- Trace: `npx playwright show-trace <trace.zip>`
