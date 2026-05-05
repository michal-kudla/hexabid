# Tytuł
Aktywacja szkicu aukcji i edukacyjne rozdzielenie reguł licytacji od rozliczenia.

## Podsumowanie (TLDR)
- Utworzenie aukcji przez SPA zapisuje szkic, a sprzedający uruchamia go osobnym endpointem `POST /api/auctions/{auctionId}/activate`.
- Aktywacja wykonuje przejście `DRAFT -> PUBLISHED -> IN_PROGRESS`, tylko dla sprzedającego i tylko gdy termin końca aukcji jest w przyszłości.
- Pełna płatność jest regułą fazy `SETTLEMENT`; nie blokuje startu aukcji ani składania ofert.
- UI szczegółów aukcji, formularz wystawiania, panel reguł i formularz dokumentów wyjaśniają różnicę między kopiami dokumentów dla udziału a oryginałami wymaganymi przy rozliczeniu.
- Business E2E tworzy szkic jako sprzedający dev i potwierdza, że aktywacja odblokowuje status `Aktywna`.
- Ten sam business E2E przełącza sesję na licytującego dev i sprawdza przyjęcie oferty przez WebSocket.

## Tagi
#auction-lifecycle #rules #documents #settlement #spa #e2e #rest-api #websocket

## Treść
Problem: po utworzeniu aukcji użytkownik widział formularz licytacji z komunikatem sugerującym, że aukcja jest zamknięta albo zablokowana przez warunki rozliczenia. W domenie istniały przejścia `publish()` i `start()`, ale nie było use case'a, REST API ani UI pozwalającego sprzedającemu uruchomić szkic.

Decyzja:
1. Dodać use case `ActivateAuctionUseCase` w `hexabid-core`, z komendą i wynikiem błędów domenowych.
2. Wystawić endpoint `POST /api/auctions/{auctionId}/activate` w kontrakcie OpenAPI i adapterze REST.
3. Aktywację ograniczyć do aktualnego sprzedającego aukcji; obcy użytkownik dostaje `403`, brak aukcji `404`, a niepoprawny stan `400`.
4. Zapis aukcji wykonywać przed publikacją eventów, zgodnie z wzorcem pozostałych use case'ów.
5. Adapter WebSocket serializuje generycznie eventy cyklu życia aukcji, dzięki czemu `AuctionPublishedEvent` i `AuctionStartedEvent` nie powodują błędu REST.
6. W UI traktować `FULL_PAYMENT_SETTLEMENT` jako blokadę rozliczenia po wygranej, a nie blokadę startu lub licytacji.
7. Kopia dokumentu wystarcza jako sygnał gotowości lub warunek udziału, jeśli reguła uczestnictwa tego wymaga; oryginał jest komunikowany jako wymóg rozliczenia po wygranej.
8. WebSocket bidding musi akceptować tę samą sesję dev/OAuth2 co REST. Handshake zapisuje każde nieanonimowe `Authentication`, a handler licytacji rozpoznaje `OAuth2User` po atrybutach `partyId`, `provider`, `subject`, `displayName`, `email`.

Walidacja 2026-05-05:
- `mvn -Plocal -DskipTests install`
- `systemctl --user restart hexabid-backend`
- `npm run e2e:auth` 1/1 pass
- `npm run e2e:business` 5/5 pass, w tym scenariusz tworzenia szkicu, aktywacji aukcji i licytacji po przelogowaniu
- `npm run build` pass z istniejącymi ostrzeżeniami Angular compiler

Powiązania:
- [[decisions/2026-05-05-dev-auth-e2e]]
- [[decisions/2026-05-05-local-port-configuration]]
- [[decisions/2026-05-04-spa-e2e-business-flow]]
