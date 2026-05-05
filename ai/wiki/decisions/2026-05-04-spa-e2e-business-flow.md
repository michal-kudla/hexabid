# Tytuł
Reprojekt testów E2E SPA na scenariusze biznesowe click-through.

## Podsumowanie (TLDR)
- Dodano scenariusze E2E oparte o pełne flow użytkownika w GUI (market -> details -> pricing, sell flow, rules/documents).
- Testy przestały być "miękkie" (conditional pass); brak danych seedowanych lub brak elementu krytycznego kończy się fail.
- Włączono automatyczne artefakty diagnostyczne Playwright: screenshot, trace i video przy failu.

## Tagi
#e2e #playwright #spa #business-flow #regresja

## Treść
Celem było zbliżenie użyteczności frontendowych e2e do `hexabid-integration-tests` po stronie backendu: szybka walidacja czy główne procesy biznesowe działają po większych zmianach.

Zakres zmian:
1. Nowy test `hexabid-spa/e2e/business-flow.spec.ts`:
   - produkt: katalog + filtrowanie strategii trackingu,
   - inventory: formularz tworzenia partii produktu,
   - aukcja: wejście na rynek i przejście do szczegółów pierwszej aukcji,
   - pricing: nawigacja do kalkulacji ceny i reguł rozliczenia,
   - reguły/dokumenty: weryfikacja panelu reguł i formularza dokumentów,
   - sprzedający: formularz wystawiania i walidacja sekcji pricing.
2. Konfiguracja Playwright (`playwright.config.ts`):
   - `trace: retain-on-failure`,
   - `screenshot: only-on-failure`,
   - `video: retain-on-failure`.
3. Dokumentacja uruchomienia i zasad tworzenia scenariuszy:
   - `hexabid-spa/e2e/README.md`.

Powiązania:
- [[decisions/2026-04-21-pricing-spa-frontend]]
- [[concepts/pricing-spa-layers]]
