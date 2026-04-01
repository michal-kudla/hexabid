# Hexabid — specyfikacja rozwojowa (zalążek)

**Data:** 2026-04-01  
**Status:** Draft v0.1 (dokument planistyczny)  
**Cel dokumentu:** zdefiniowanie docelowego kierunku rozwoju Hexabid jako **wzorcowej, edukacyjnej i jednocześnie funkcjonalnej aplikacji aukcyjnej**, z naciskiem na archetypy domenowe, architekturę heksagonalną i wysoką jakość techniczną.

---

## 1) Dlaczego nowa specyfikacja

Obecne dokumenty (`architecture-c4`, `user-guide`, raporty) dobrze opisują **stan aktualny** i wybrane luki. Potrzebny jest jednak dokument typu **specyfikacja rozwojowa** odpowiadający na pytanie:

- co analizujemy,
- co projektujemy,
- co implementujemy,
- w jakiej kolejności,
- oraz po czym poznamy, że system jest „kompletny” produktowo i edukacyjnie.

Ten dokument pełni rolę punktu startowego dla roadmapy i backlogu.

---

## 2) Ocena zgodności obecnego Hexabid z archetypami

## 2.1. Co już jest zgodne i wartościowe

Na bazie obecnej dokumentacji i kodu, Hexabid jest dobrze osadzony w nowoczesnym fundamencie:

- architektura heksagonalna (porty i adaptery),
- modularny podział bounded-context/service-modules,
- contract-first (OpenAPI),
- czysta domena aukcyjna w rdzeniu,
- adaptery integracyjne (KYC, Kafka, płatności),
- testy architektoniczne,
- real-time bidding przez WebSocket,
- profile deweloperskie i lokalne implementacje części portów.

## 2.2. Gdzie już teraz widać potrzebę „kroku w tył”

„Krok w tył” jest uzasadniony, ale nie jako cofanie implementacji — raczej jako **uporządkowanie rozwoju przez specyfikację**. Główne luki architektoniczno-produktowe:

1. Brak pełnego domknięcia procesu po wygranej aukcji (settlement/payment lifecycle).
2. Brak jednolitego modelu dla towarów jednostkowych i wolumenowych (batch/quantity).
3. Brak formalnego katalogu feature’ów ofert (kaucja, wymagane uprawnienia, dokumenty).
4. Brak pełnej specyfikacji „dev ports” dla lokalnego developmentu (w tym płatności, KYC, notyfikacje).
5. Brak precyzyjnych etapów i kryteriów Definition of Done dla kolejnych capability.

**Wniosek:** obecny rozwój jest częściowo zgodny z archetypami, ale przed dalszym skalowaniem funkcji potrzebna jest docelowa specyfikacja i fazowanie prac.

---

## 3) Wizja produktu

Hexabid ma być:

1. **Aplikacją wzorcową edukacyjnie** – pokazującą nowoczesne techniki budowy systemów (DDD, hexagonal, event-driven, contract-first, testowalność, observability).
2. **Aplikacją funkcjonalną biznesowo** – umożliwiającą realne przejście przez procesy aukcyjne end-to-end.
3. **Aplikacją przyjazną deweloperowi lokalnemu** – z lokalnymi adapterami portów i szybkim feedback-loopem testowym.

---

## 4) Zakres domenowy (v1+)

## 4.1. Procesy obowiązkowe (E2E)

1. Pozyskanie produktu/partii.
2. Utworzenie oferty aukcyjnej.
3. Licytacja i rozstrzygnięcie.
4. Rozliczenie po wygranej (płatność + timeout + kompensacje).
5. Zmiana stanu magazynowego/sprzedażowego.
6. Audyt i historia procesu.

## 4.2. Typy oferowanych dóbr

Należy wspierać dwa tryby:

1. **Egzemplarz unikalny (quantity=1)** — klasyczny model produktu jednostkowego.
2. **Partia/wolumen (batch)** — np. 10 ton ziemniaków, z możliwością sprzedaży częściowej wielu klientom.

Wymagane konsekwencje modelowe:

- identyfikowalność pochodzenia (`batchId`, atrybuty partii),
- sprzedaż częściowa i kontrola dostępnej ilości,
- spójna polityka rezerwacji i zwalniania wolumenu,
- możliwość raportowania „z jakiej partii pochodzi sprzedana ilość”.

## 4.3. Feature’y oferty (Offer Features)

Oferta musi wspierać konfigurację warunków dodatkowych, np.:

- kaucja/depozyt (np. 10%),
- wymagane dokumenty uprawniające do zakupu (np. alkohol),
- opcjonalne reguły dostawy/odbioru,
- ograniczenia nabywcy (np. wymagane statusy weryfikacji).

Te cechy powinny być modelowane jako **jawny mechanizm feature flags / policy attachments** do oferty, a nie twardo zakodowane wyjątki.

---

## 5) Docelowe archetypy i decyzje architektoniczne

## 5.1. Archetypy obowiązkowe

1. **Hexagonal Architecture** (porty wejścia/wyjścia, czysta domena).
2. **Event-Driven Core** (zdarzenia domenowe jako fakty biznesowe).
3. **Saga / Process Manager** dla długich transakcji post-auction.
4. **Payment Intent Lifecycle + idempotencja webhooków**.
5. **Audit Journal** (append-only, correlation IDs).
6. **Batch/Quantity archetype** dla partii i sprzedaży częściowej.

## 5.2. Polityka portów i adapterów developerskich

Każdy kluczowy port zewnętrzny musi mieć co najmniej:

- adapter produkcyjny (lub integracyjny),
- adapter lokalny/deweloperski (in-memory/stub/fake).

Dotyczy to szczególnie:

- bramki płatności,
- KYC,
- notyfikacji,
- publikacji zdarzeń,
- persystencji read modeli.

---

## 6) Niefunkcjonalne wymagania jakościowe

1. **Szybkie testy jednostkowe i przekrojowe bez I/O**:
   - preferowane in-memory repository (np. HashMap) dla testów core,
   - testy domenowe bez uruchamiania Springa.
2. **Deterministyczność i idempotencja** procesów płatności/sagi.
3. **Wysoka obserwowalność** (trace/correlation/audit).
4. **Czytelna, „krzycząca” domena** i język ubiquitous.
5. **Kontrakty API utrzymywane contract-first** i wersjonowanie zmian.

---

## 7) Proponowane etapy realizacji

## Etap 0 — Specyfikacja i model domeny (obecny krok)

- ujednolicenie słownika domenowego,
- opis bounded contexts,
- decyzje: unique item vs batch, payment lifecycle, offer features,
- Definition of Done dla etapów 1..N.

## Etap 1 — Domknięcie transakcji po wygranej (MVP kompletności)

- settlement orchestrator,
- payment intent,
- timeout i kompensacje,
- audyt decyzji i stanów końcowych.

## Etap 2 — Batch i sprzedaż częściowa

- model partii i ilości,
- rezerwacja/zwalnianie ilości,
- śledzenie pochodzenia wolumenu,
- scenariusze wieloklienckie.

## Etap 3 — Feature’y ofert i policy engine

- kaucja,
- wymagane dokumenty,
- walidacje pre-bid / pre-purchase,
- rozszerzalność przez konfiguracje reguł.

## Etap 4 — Developer Experience i adaptery lokalne

- komplet lokalnych implementacji portów,
- profile uruchomieniowe i dane demo,
- gotowe scenariusze testowe dla warsztatów edukacyjnych.

## Etap 5 — Archetypy rozszerzające

- notyfikacje wielokanałowe,
- reputacja/rating,
- reklamacje (case management),
- discovery/reporting.

---

## 8) Wstępny podział na bounded contexts

1. **Auctioning** – tworzenie ofert, licytacja, rozstrzygnięcia.
2. **Catalog & Inventory** – produkt, partia, ilość, dostępność.
3. **Settlement & Payments** – rozliczenie wygranych aukcji.
4. **Identity & Trust** – użytkownicy, role, KYC, uprawnienia.
5. **Compliance & Audit** – historia decyzji, ślad audytowy.
6. **Offer Policies** – feature’y ofert i reguły kwalifikacyjne.

---

## 9) Kryteria gotowości (Definition of Done — poziom systemu)

System uznajemy za „kompletny” na poziomie referencyjnym, gdy:

1. Obsługuje E2E od wystawienia do rozliczenia i zdjęcia ze stanu.
2. Wspiera zarówno aukcje jednostkowe, jak i batch/partial sale.
3. Potrafi egzekwować feature’y i wymagania formalne oferty.
4. Posiada lokalne adaptery kluczowych portów dla pracy developerskiej.
5. Dostarcza szybki zestaw testów bez I/O dla krytycznych procesów.
6. Każdy kluczowy proces jest audytowalny i odtwarzalny.

---

## 10) Backlog startowy (proponowany)

1. Spec domenowy: `Auction`, `Lot`, `Batch`, `OfferFeature`, `Settlement`.
2. Event map procesu post-auction (success/failure/timeout/refund/dispute).
3. Kontrakt `PaymentGatewayPort` + lokalny adapter developerski.
4. In-memory adaptery dla testów przekrojowych rdzenia.
5. Macierz reguł kwalifikacyjnych zakupu (KYC + dokumenty + feature’y).
6. Minimalny audyt append-only z correlation id.

---

## 11) Relacja do istniejących dokumentów

Ten dokument **nie zastępuje**:

- `doc/architecture-c4.adoc` (opis architektury obecnego rozwiązania),
- `doc/user-guide.adoc` (instrukcja użycia istniejących funkcji),
- raportów analitycznych z `doc/reports`.

Ten dokument jest warstwą nadrzędną: **specyfikacją rozwojową** służącą planowaniu kolejnych iteracji.

---

## 12) Następny krok

Po akceptacji v0.1 należy przygotować v0.2 zawierającą:

1. tabelę decyzji architektonicznych (ADR-lite),
2. model procesów BPMN/EventStorming dla E2E,
3. listę use case’ów z kryteriami akceptacji,
4. mapowanie wymagań na moduły i kontrakty API.
