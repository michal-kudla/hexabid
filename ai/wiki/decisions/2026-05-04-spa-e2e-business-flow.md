# Tytuł
Reprojekt testów E2E SPA na scenariusze biznesowe click-through.

## Podsumowanie (TLDR)
- Dodano scenariusze E2E oparte o pełne flow użytkownika w GUI (market -> details -> pricing, sell flow, rules/documents).
- Testy przestały być "miękkie" (conditional pass); brak danych seedowanych lub brak elementu krytycznego kończy się fail.
- Chronione endpointy mogą zakończyć się kontrolowanym stanem auth w GUI (np. pricing bez sesji), ale ekran i komunikat muszą być jawnie zweryfikowane.
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
   - pricing: nawigacja do kalkulacji ceny; test akceptuje pełną kalkulację po auth albo kontrolowany empty-state wymagający logowania,
   - reguły/dokumenty: weryfikacja panelu reguł i formularza dokumentów,
   - sprzedający: formularz wystawiania i walidacja sekcji pricing.
2. Konfiguracja Playwright (`playwright.config.ts`):
   - `baseURL` i `webServer` używają `localhost`, aby działać z dev serverem słuchającym na IPv6 loopback `::1`,
   - `trace: retain-on-failure`,
   - `screenshot: only-on-failure`,
   - `video: retain-on-failure`.
3. Dokumentacja uruchomienia i zasad tworzenia scenariuszy:
   - `hexabid-spa/e2e/README.md`.

Walidacja lokalna 2026-05-05:
- Backend należy budować przez Maven profile: `mvn -Plocal -DskipTests install`.
- Backend należy restartować jako user-service: `systemctl --user restart hexabid-backend`.
- `hexabid-backend.service` musi używać `--spring.profiles.active=local`, nie `dev`, inaczej seed aukcji nie jest dostępny dla E2E.
- `npm run e2e:business` przechodzi lokalnie: 4/4.

Powiązania:
- [[decisions/2026-04-21-pricing-spa-frontend]]
- [[concepts/pricing-spa-layers]]
- [[PROFIL_LOCAL_GUIDE]]
