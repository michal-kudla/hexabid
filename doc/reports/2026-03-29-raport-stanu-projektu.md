# Hexabid — raport stanu projektu (baseline)

**Data raportu:** 2026-03-29  
**Cel dokumentu:** punkt odniesienia („baseline”) do cyklicznej oceny dojrzałości projektu i planowania kolejnych usprawnień.

---

## 1) Metoda oceny i zakres

Raport ocenia projekt w czterech osiach:

1. **Dopasowanie do deklarowanego celu produktu** (aukcje, architektura heksagonalna, contract-first, real-time).
2. **Dojrzałość techniczna i operacyjna** (testy, bezpieczeństwo, obserwowalność, niezawodność).
3. **Gotowość do współpracy z agentami AI** (machine-readable contracts, przewidywalność interfejsów, idempotencja, audytowalność, automatyzowalność).
4. **Jakość planu rozwoju** (co poprawić, w jakiej kolejności i jak mierzyć postęp).

Wnioski oparto o:
- kod i dokumentację projektu,
- uznane źródła branżowe (OpenAPI Initiative, OWASP, 12-Factor, MCP).

---

## 2) Czy aplikacja podąża za swoim wyznaczonym celem?

## Krótka odpowiedź

**Tak — w dużej mierze tak.**  
Projekt konsekwentnie realizuje deklarowany cel „wzorcowej aplikacji aukcyjnej” opartej o **hexagonal architecture + contract-first + domenę**. Jednocześnie ma kilka luk produkcyjnych (bezpieczeństwo WS, fallbacki/resilience, płatności, polityka wersjonowania API), które ograniczają dojrzałość.

## Dowody „za”

1. **Cel i architektura są jasno zdefiniowane w repo**
   - README deklaruje architekturę heksagonalną, contract-first i podział na moduły zgodny z ports-and-adapters.
2. **Rdzeń domeny jest oddzielony od frameworka**
   - ArchUnit pilnuje, aby `core` nie zależał od Spring/JPA.
3. **Kontrakty API są jawne i utrzymywane w OpenAPI**
   - osobne kontrakty dla aukcji/auth/płatności,
   - adapter REST implementuje wygenerowany delegate.
4. **Scenariusze real-time są zaadresowane**
   - WebSocket obsługuje licytację i kanały zdarzeń/błędów,
   - zdarzenia domenowe są publikowane także do Kafki.
5. **Tożsamość użytkownika pochodzi z kontekstu auth, a nie z payloadu klienta**
   - to istotne dla integralności domeny i bezpieczeństwa reguł biznesowych.

## Ograniczenia wobec celu

1. **Warstwa płatności wygląda na częściowo demonstracyjną**
   - `ProcessPaymentUseCase` tworzy callback hardcoded i komentarze „in a real system...”,
   - listener używa `System.out.println`.
2. **Konfiguracja security jest zbyt liberalna dla produkcji**
   - globalnie wyłączony CSRF,
   - `ws-auctions/**` dopuszczone `permitAll`.
3. **Repo zawiera artefakty build (`target/`)**
   - utrudnia to utrzymanie, review i automatyzację.

---

## 3) Gotowość do pracy z agentami (AI/LLM agents)

## Ocena syntetyczna

**Poziom: 6/10 (średni, dobra baza techniczna, brak „agent-first hardening”).**

## Co już wspiera agentów

1. **Machine-readable contracts (OpenAPI)** — bardzo dobry fundament pod agentowe integracje narzędziowe.
2. **Jawne use-case’y i porty wejściowe** — deterministyczne punkty orkiestracji.
3. **Czytelne kody odrzuceń biznesowych** (np. `BID_AMOUNT_TOO_LOW`, `CONCURRENT_MODIFICATION`) — agent może podejmować decyzje naprawcze.
4. **Oddzielenie auth od payloadu** — redukuje powierzchnię błędów semantycznych po stronie automatyzacji.

## Co blokuje „agent readiness”

1. **Brak dedykowanego interfejsu agentowego**
   - brak MCP server / tool schema / function catalog dla stabilnych operacji domenowych.
2. **Brak idempotency contract dla komend write**
   - przy automatyzacji (retry) rośnie ryzyko duplikacji operacji.
3. **Brak formalnych SLO i semantyki retry/timeouts per integracja**
   - np. KYC adapter rzuca `IllegalStateException` bez polityki degradacji.
4. **Niedookreślona wersjonowalność API i kompatybilność kontraktów**
   - brak jawnej polityki semver/deprecation w dokumentacji.
5. **Brak warstwy „audit trail” dla decyzji agentowych**
   - jest eventing biznesowy, ale nie ma standardu śledzenia: kto/czemu/na jakiej podstawie wykonał akcję automatyczną.

---

## 4) Co jest dobre (mocne strony)

1. **Spójna architektura modułowa**
   - podział na core, adaptery in/out, bootstrap jest logiczny i konsekwentny.
2. **Czysty model domenowy i use-case centric design**
   - reguły biznesowe trzymane blisko domeny.
3. **Dobre rozdzielenie kanałów synchronicznych i asynchronicznych**
   - REST do odczytów/komend, WebSocket/Kafka do eventów.
4. **Testy domenowe najważniejszych use-case’ów istnieją**
   - create/placeBid/closeExpired mają testy jednostkowe.
5. **ArchUnit jako „bariera regresji architektonicznej”**
   - to bardzo dobra praktyka długoterminowa.

---

## 5) Co jest słabe / ryzykowne

### 5.1 Bezpieczeństwo

- `csrf().disable()` + szerokie `permitAll` dla WS i H2 console to konfiguracja deweloperska, nie produkcyjna.
- Brak jawnych zabezpieczeń typowych dla WebSocket security hardening (origin policy, ograniczenia wiadomości, rate limits, walidacja payload limits, telemetry security).

### 5.2 Niezawodność integracji

- KYC adapter przy błędzie transportowym eskaluje `IllegalStateException`; brak retry/backoff/circuit breaker.
- Brak jawnej klasyfikacji błędów zewnętrznych na „retryable / non-retryable”.

### 5.3 Płatności

- Logika płatności ma ślady „demo mode” (hardcoded callback, komentarze o przyszłej implementacji, brak trwałości księgowań).

### 5.4 Operacyjność

- Brak widocznych metryk domenowych/SLO/SLI w repo (np. skuteczność licytacji, odsetek konfliktów optimistic lock, błędy KYC).
- Brak formalnego runbooka incydentowego dla najważniejszych failure modes.

### 5.5 Higiena repo

- Obecność wygenerowanych artefaktów build (`target/...`) sugeruje ryzyko „szumu” w PR i trudniejszego utrzymania.

---

## 6) Co poprawić i jak (plan działań)

## Priorytet P0 (0–2 tygodnie)

1. **Twarde minimum security dla produkcji**
   - rozdziel profile `dev` / `prod` dla security,
   - przywróć ochronę CSRF tam, gdzie ma zastosowanie,
   - dodaj `Origin` validation, ograniczenia payload size i throttling dla WebSocket.

2. **Higiena repo i pipeline**
   - usuń artefakty build z VCS,
   - doprecyzuj `.gitignore`,
   - w CI dodaj gate: build + test + archunit + static checks.

3. **Obsługa błędów KYC**
   - wprowadź retry policy (z limitem), timeouty, fallback i czytelne mapowanie błędów na odpowiedzi biznesowe.

## Priorytet P1 (2–6 tygodni)

4. **Agent Interface Layer**
   - przygotuj stabilny „tooling facade” (MCP lub dedykowane endpointy agentowe),
   - zdefiniuj minimalny zestaw operacji: browse, get details, create auction, place bid,
   - dodaj jawny kontrakt idempotency key dla operacji write.

5. **Observability pod automatyzację**
   - metryki domenowe + tracing korelacyjny (requestId/sessionId/agentActionId),
   - dashboard „auction health” i alarmy.

6. **Polityka wersjonowania API i kontraktów**
   - semver dla kontraktów,
   - deprecation policy,
   - kontraktowe testy kompatybilności.

## Priorytet P2 (6–12 tygodni)

7. **Domknięcie modułu płatności**
   - trwałość księgowań,
   - outbox/inbox dla gwarancji dostarczenia,
   - pełen model kompensacji/reconciliation.

8. **Runbook i governance agentów**
   - standard audytu działań automatycznych,
   - polityka uprawnień i ograniczeń agentów,
   - scenariusze rollback/replay.

---

## 7) KPI do kolejnych raportów

Proponowane metryki trendowe (miesięcznie/kwartalnie):

1. **Architecture Integrity Score**
   - % modułów zgodnych z regułami ArchUnit,
   - liczba naruszeń boundary.

2. **API Contract Health**
   - # breaking changes,
   - % endpointów z testami kontraktowymi,
   - średni czas od deprecacji do usunięcia.

3. **Auction Reliability**
   - p95 latency: `createAuction`, `placeBid`,
   - conflict rate (`CONCURRENT_MODIFICATION`),
   - error budget dla integracji KYC.

4. **Agent Readiness Score**
   - % operacji write z idempotency,
   - % operacji z jednoznaczną klasyfikacją retry,
   - % akcji automatycznych z pełnym audit trail.

---

## 8) Podsumowanie zarządcze

Hexabid ma **solidny fundament architektoniczny** i jest spójny z deklarowanym celem „wzorcowej aplikacji heksagonalnej”.  
Największa luka nie dotyczy domeny aukcyjnej, tylko **dojrzałości produkcyjnej i agent-first operability**: bezpieczeństwo WebSocket/profili, odporność integracji, idempotencja i audyt decyzji automatycznych.

Jeśli zrealizować plan P0 + P1, projekt może przejść z poziomu „dobre demo/solidny szkielet” do „produkcyjnego systemu gotowego do bezpiecznej automatyzacji agentowej”.

---

## 9) Źródła projektu (evidence)

- `README.md`
- `doc/architecture-c4.adoc`
- `doc/user-guide.adoc`
- `auctions-architecture-tests/src/test/java/com/acme/auctions/architecture/CoreArchitectureTest.java`
- `auctions-api-contract/src/main/resources/openapi/auction-api.yaml`
- `auctions-api-contract/src/main/resources/openapi/auth-api.yaml`
- `auctions-api-contract/src/main/resources/openapi/payment-api.yaml`
- `auctions-bootstrap/src/main/java/com/acme/auctions/bootstrap/AuctioningConfiguration.java`
- `auctions-adapter-in-rest/src/main/java/com/acme/auctions/adapter/in/rest/RestAuctionApiDelegate.java`
- `auctions-adapter-in-ws/src/main/java/com/acme/auctions/adapter/in/ws/AuctionBiddingWebSocketController.java`
- `auctions-adapter-in-auth-oauth/src/main/java/com/acme/auctions/adapter/in/auth/oauth/OAuth2SecurityConfiguration.java`
- `auctions-adapter-out-kyc/src/main/java/com/acme/auctions/adapter/out/kyc/GeneratedKycClientAdapter.java`
- `auctions-payment-core/src/main/java/com/acme/auctions/payment/core/usecase/ProcessPaymentUseCase.java`
- `auctions-payment-core/src/main/java/com/acme/auctions/payment/core/infrastructure/AuctionWonEventListener.java`

## 10) Źródła zewnętrzne (best practices / uznane standardy)

1. OpenAPI Specification 3.0.3 (OpenAPI Initiative): https://spec.openapis.org/oas/v3.0.3
2. OWASP WebSocket Security Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/WebSocket_Security_Cheat_Sheet.html
3. OWASP API Security Top 10 (2023): https://owasp.org/API-Security/
4. The Twelve-Factor App (Config): https://12factor.net/config
5. Model Context Protocol — Specification: https://modelcontextprotocol.io/specification
6. Hexagonal Architecture (Alistair Cockburn): https://alistair.cockburn.us/hexagonal-architecture

