# LLM WIKI - Log Zmian

Chronologiczny zapis wszystkich istotnych zmian, decyzji i postępów w projekcie Hexabid.

## Format wpisu
```
## [DATA] [TYP] Opis zmiany
- Szczegóły decyzji/wdrożenia
- Linki do powiązanych dokumentów: [[nazwa-pliku]]
- Tagi: #tag1 #tag2
```

---

## [2026-04-16] [ARCHITECTURE] Refaktoryzacja modułów i pakietów
- Zmieniono nazwy wszystkich modułów z `auctions-*` na `hexabid-*`
- Migracja pakietów z `com.acme.auctions` na `com.github.hexabid`
- Regeneracja OpenAPI kontraktów z nowymi pakietami
- Link: [[decisions/2026-04-16-module-refactoring]]

## [2026-04-16] [CONFIGURATION] System profili Maven/Spring
- Zmieniono profil Maven z `dev` na `local` (aktywny domyślnie)
- Spójne nazewnictwo profili: `local-auth`, `local-kyc`, `local-payment`
- Konfiguracja Spring profilu `local` z portem 18080 i seed data
- Link: [[decisions/2026-04-16-profiles-local]]

## [2026-04-16] [DOCUMENTATION] Organizacja dokumentacji
- Utworzony katalog `ai/` dla dokumentacji agentów AI
- Przeniesiony PROFIL_LOCAL_GUIDE.md do `ai/`
- Utworzona struktura LLM WIKI w `ai/wiki/`
- Link: [[decisions/2026-04-16-documentation-structure]]

## [2026-04-17] [ARCHITECTURE] Architektura ceny - moduł hexabid-pricing
- Decyzja o wprowadzeniu modułu `hexabid-pricing` z archetypami M03 (Pricing) + M04 (Rules)
- Modelowanie ceny jako kompozycja składowych: hammer price, wadium, akcyza, cło, VAT
- Trójwarstwowa architektura: core (stabilny), domknięcia (policy), fabryka (selection)
- Drzewo komponentów (CompositePriceComponent) z zależnościami między składnikami
- Typy produktów (UNIQUE vs BATCH/IDENTICAL) wpływają na calculatory i interpretacje
- Scenariusze E2E i endpointy dla aukcji z wadium, akcyzą, cłem i VAT
- Link: [[decisions/2026-04-17-pricing-architecture]]

---

*Automatycznie aktualizowane przez agentów AI przy każdej istotnej zmianie.*

## [2026-04-21] [IMPLEMENTATION] Pricing integration tests + REST adapter
- Napisano 22 nowe scenariusze testowe IT_P10-IT_P31 w `PricingScenariosExtendedIT`
- Zakres: imported+excisable car, per-unit excise, fixed wadium, zero VAT, reduced VAT 5%/8%, sum verification, consistency, customs-only, excise-only, full lifecycle
- Zaimplementowano 3 endpointy w `RestAuctionApiDelegate`: `getAuctionPrice`, `depositWadium`, `refundWadium`
- Naprawiono bug w testach: `setBasePath` -> `updateBaseUri` w wygenerowanym kliencie API
- Skonfigurowano systemd user services: `hexabid-backend`, `hexabid-spa`
- Wynik testów: 34/35 pass (1 pre-existing failure w ProductBatchInstanceAuctionScenariosIT)
- Link: [[decisions/2026-04-17-pricing-architecture]]
- Tagi: #pricing #integration-tests #systemd #rest-adapter

## [2026-04-21] [IMPLEMENTATION] Pricing SPA frontend module
- Dodano moduł pricing do Angular SPA: strona kalkulacji ceny, wpłata/zwrot wadium
- Nowe pliki data-access: `pricing-api.models.ts`, `pricing-view.mapper.ts`, `pricing-api.service.ts`
- Nowy feature: `features/pricing/` z `PricingFacade`, `PricingPageComponent`
- Rozszerzono stronę tworzenia aukcji o `PricingConfig` (wadium, VAT, akcyza, cło)
- Dodano link "Zobacz kalkulację ceny" na stronie szczegółów aukcji
- Nowa ruta: `/auction/:auctionId/pricing`
- E2E tests: `pricing.spec.ts`, `pricing-create.spec.ts`, `pricing-navigation.spec.ts` (13 testów)
- Link: [[decisions/2026-04-21-pricing-spa-frontend]]
- Tagi: #pricing #spa #angular #e2e #frontend

## [2026-04-24] [DESIGN] SPA Visual Redesign — Concept B (Warm Professional)
- Pełny redesign look-and-feel SPA: z "glossy" (gradienty, glow, 1px borders) na "Warm Professional" (flat buttons, 2px borders, WCAG AA)
- Nowa paleta: Stone (#1f1a12 ink) + Amber (#a86514 accent) zamiast Blue + Orange
- Nowe fonty: DM Sans (body) + Source Serif 4 (display) zamiast Manrope + Space Grotesk
- Nowa architektura CSS: `src/styles/_tokens.scss` (jedno źródło prawdy), `_reset.scss`, `_components.scss`, `_utilities.scss`
- Dostępność: 2px borders, min-height 44px, badge z border, alert z border-left:4px, focus ring 4px
- Nawigacja: underline-based (bottom-border) zamiast pill-segmented
- Mockupy: `.local/mockups/concept-a-clinical.html`, `concept-b-warm-professional.html`, `concept-c-nordic-calm.html`
- Redesign proposal: `.local/mockups/REDESIGN-PROPOSAL.md`
- Link: [[decisions/2026-04-24-spa-visual-redesign]]
- Tagi: #spa #design #accessibility #css #tokens #redesign

## [2026-05-02] [IMPLEMENTATION] Konfigurowalny System Regul Aukcyjnych (Fazy 1-4)
- Zaimplementowano plan z `ai/wiki/plan-system-regul.adoc` -- Fazy 1-4
- **Faza 1 (hexabid-rules)**: AST + 3 algebry (Boolean, Compliance, ExplainedAlgebra) + RuleEngine + RuleCatalog + 124 testy
- **Faza 2 (hexabid-core)**: Rozszerzony cykl aukcji (DRAFT→PUBLISHED→IN_PROGRESS→PENDING_SETTLEMENT→SETTLED/FAILED_SETTLEMENT/REOFFERED), 6 nowych domain events, SettleAuctionService, reservePrice enforcement
- **Faza 3 (hexabid-core)**: Modele dokumentów (DocumentType/Status/Requirement), nowe porty (DocumentRepository, WadiumDepositPort, AuctionRuleEvaluator), SubmitDocumentService
- **Faza 4 (hexabid-rules-adapter)**: Nowy moduł Maven z RuleEvaluatorAdapter + RulesConfiguration (12 reguł biznesowych: wadium, pelnoletność, akcyza, cło, nieruchomości), integracja PlaceBidService z AuctionRuleEvaluator, DevDocumentRepository + DevWadiumDeposit stuby
- Nowe moduły Maven: `hexabid-rules-adapter` (dodany do root pom, BOM, bootstrap)
- Zmieniono `AuctionStatus.OPEN` → `IN_PROGRESS`, `CLOSED` zachowane
- Link: [[plan-system-regul]]
- Tagi: #rules #ast #algebra #auction-lifecycle #settlement #documents #wadium

## [2026-05-04] [IMPLEMENTATION] Rules REST API + SPA GUI
- Dodano 2 nowe endpointy REST: `GET /api/auctions/{id}/rules?phase=` + `POST /api/auctions/{id}/documents`
- Rozszerzono `RuleViolation` o pola `status` (SATISFIED/PENDING/VIOLATED) i `severity` (BLOCKING/WARNING/INFORMATIVE)
- Zaktualizowano OpenAPI YAML: nowe schematy RuleEvaluationResponse, RulePhaseEvaluation, RuleViolationItem, RulePhase, RuleStatus, RuleSeverity, SubmitDocumentRequest/Response, DocumentType, DocumentStatus
- Regeneracja TypeScript client z nowymi typami reguł
- Nowe komponenty SPA: `RulesPanelComponent` (3-phase rules display), `DocumentSubmitComponent` (document submission form), `RulesFacade` (rules state management)
- Integracja rules panel na stronach: Auction Details (PARTICIPATION + BIDDING) i Pricing (SETTLEMENT)
- Nowe pliki data-access: `rules-api.models.ts`, `rules-view.mapper.ts`, `rules-api.service.ts`
- Naprawiono bug: `AuctionStatus.OPEN` → `AuctionStatus.IN_PROGRESS` w mapper i home page
- Testy integracyjne: `RulesEvaluationIT` (R1.1-R1.7 rules evaluation, R2.1-R2.2 document submission, R3.1-R3.2 security)
- Testy e2e Playwright: `rules.spec.ts` (rules panel visibility, document form, settlement rules, status indicators)
- Tagi: #rules #spa #rest-api #openapi #e2e #integration-tests

## [2026-05-04] [IMPLEMENTATION] SPA E2E jako scenariusze biznesowe click-through
- Dodano nowy zestaw Playwright: `hexabid-spa/e2e/business-flow.spec.ts` (market->details->pricing, sell flow, rules/documents)
- Testy są "strict": brak aukcji demo lub brak krytycznego elementu UI powoduje fail (bez cichego pomijania scenariusza)
- Włączono artefakty diagnostyczne Playwright na fail: trace + screenshot + video
- Dodano przewodnik uruchamiania i zasad: `hexabid-spa/e2e/README.md`
- Rozszerzono scenariusze o Product + Inventory (katalog/filtrowanie + formularz tworzenia partii) oraz step-level screenshot attachments
- Link: [[decisions/2026-05-04-spa-e2e-business-flow]]
- Tagi: #spa #e2e #playwright #business-flow #regresja

## [2026-05-05] [VERIFICATION] Lokalna walidacja SPA E2E przez systemd backend
- Zbudowano backend z profilem Maven `local`: `mvn -Plocal -DskipTests install`
- Zmieniono user-service `hexabid-backend` z poprzedniego profilu developerskiego na `--spring.profiles.active=local` i zrestartowano przez `systemctl --user restart hexabid-backend`
- Naprawiono konfigurację Playwright na `localhost:14200`, ponieważ lokalny Angular dev server słuchał na IPv6 loopback `::1`, a `127.0.0.1` dawał `ERR_CONNECTION_REFUSED`
- Dostosowano business-flow E2E do aktualnego DOM SPA oraz jawnego stanu auth dla pricing bez sesji
- Wynik: `npm run e2e:business` 4/4 pass, `npm run build` pass z istniejącymi ostrzeżeniami Angular compiler
- Link: [[decisions/2026-05-04-spa-e2e-business-flow]]
- Tagi: #spa #e2e #playwright #systemd #local-profile

## [2026-05-05] [CONFIGURATION] Ujednolicenie lokalnych portów i nazw modułów
- Usunięto tekstowe odniesienia do starych portów z konfiguracji, kontraktów, README i dokumentacji operacyjnej
- Ustawiono lokalny backend jako `http://localhost:18080/hexabid`, SPA jako `http://localhost:14200`, WebSocket jako `ws://localhost:18080/hexabid/ws-auctions`
- Zaktualizowano OpenAPI `servers.url`, utrzymywane runtime'y TypeScript, `contract:sync`, systemd guide i local payment mock URL
- Wynik: `mvn -Plocal -DskipTests install`, `npm run build`, `systemctl --user restart hexabid-backend`, `npm run e2e:business` 4/4 pass
- Link: [[decisions/2026-05-05-local-port-configuration]]
- Tagi: #configuration #ports #local-profile #systemd #openapi

## [2026-05-05] [FIX] Dev auth login flow i E2E regresji logowania
- Naprawiono link wyboru użytkownika dev: `/login/dev` używa `/login/dev/select`, a nie callbacku `/login/oauth2/code/dev`
- Endpoint wyboru zapisuje `OAuth2AuthenticatedUser` w `SecurityContext` sesji i przekierowuje do lokalnego SPA albo bezpiecznej ścieżki względnej
- Po analizie HAR ustawiono `server.servlet.session.cookie.path=/`, żeby cookie sesji z backendu `/hexabid` było wysyłane do requestów SPA proxy `/api/me`
- Dodano Playwright smoke `hexabid-spa/e2e/dev-auth.spec.ts` oraz script `npm run e2e:auth`; test odtwarza `/oauth2/authorization/dev` -> wybór konta -> dashboard
- Zaktualizowano dokumentację E2E o uruchamianie backendu przez Maven profile + systemd service
- Wynik: `mvn -Plocal -DskipTests install`, `systemctl --user restart hexabid-backend`, `npm run e2e:auth` 1/1 pass, `npm run e2e:business` 4/4 pass, `npm run build` pass
- Link: [[decisions/2026-05-05-dev-auth-e2e]]
- Tagi: #auth #dev-auth #oauth2 #e2e #playwright

## [2026-05-05] [IMPLEMENTATION] Aktywacja szkicu aukcji i edukacyjne reguły UI
- Dodano use case `ActivateAuctionUseCase` oraz REST `POST /api/auctions/{auctionId}/activate` dla sprzedającego aukcji
- Aktywacja wykonuje przejście `DRAFT -> PUBLISHED -> IN_PROGRESS`, zapisuje aukcję i publikuje eventy cyklu życia
- Naprawiono WebSocket publisher, aby eventy `AuctionPublishedEvent` i `AuctionStartedEvent` nie powodowały błędu REST jako "Unsupported event type"
- UI szczegółów aukcji pokazuje panel uruchomienia szkicu dla sprzedającego oraz precyzyjne powody blokady licytacji
- UI reguł i dokumentów wyjaśnia, że pełna płatność i oryginały dokumentów należą do rozliczenia po wygranej, a kopie mogą wystarczyć dla udziału/licytacji
- Business E2E rozszerzono do 5 testów o scenariusz logowania sprzedającego dev, utworzenia szkicu i aktywacji aukcji
- Wynik: `mvn -Plocal -DskipTests install`, `systemctl --user restart hexabid-backend`, `npm run e2e:auth` 1/1 pass, `npm run e2e:business` 5/5 pass, `npm run build` pass
- Link: [[decisions/2026-05-05-auction-activation-rules-guidance]]
- Tagi: #auction-lifecycle #rules #documents #settlement #spa #e2e

## [2026-05-05] [FIX] WebSocket bidding używa sesji dev/OAuth2
- Zdiagnozowano błąd z HAR i kanału WebSocket: po przelogowaniu REST widział użytkownika, ale handler STOMP odrzucał ofertę jako `UNAUTHENTICATED`
- Przyczyna: handshake WebSocket zapisywał tylko principal typu `UserDetails`, a dev login używa `OAuth2User`; handler licytacji oczekiwał `Authentication` przez `@AuthenticationPrincipal`
- Poprawiono handshake, aby zapisywał każde nieanonimowe `Authentication`, oraz resolver bidderów, aby mapował `OAuth2User` na domenowy `AuthenticatedUser`
- Dodano zależność `spring-security-oauth2-core` do `hexabid-adapter-in-ws`
- Rozszerzono business E2E: sprzedający tworzy i aktywuje aukcję, następnie test loguje `bidder-ola` i składa ofertę przez WebSocket
- Wynik: `mvn -Plocal -DskipTests install`, `systemctl --user restart hexabid-backend`, `npm run e2e:business` 5/5 pass, `npm run e2e:auth` 1/1 pass, `npm run build` pass
- Link: [[decisions/2026-05-05-auction-activation-rules-guidance]]
- Tagi: #websocket #stomp #auth #dev-auth #e2e #bidding

## [2026-05-09] [DESIGN] System Zbierania Oświadczeń — DAG, szablony aukcji, kaskadowe odrzucenie
- Projekt modułu `hexabid-statements` oparty na algorytmach grafowych (DAG, sortowanie topologiczne, domknięcie przechodnie)
- 4 typy aukcji jako szablony oświadczeń: ENGLISH_ASCENDING (5), SEALED_BID_TENDER (16), RESTRICTED_TENDER (16 dwuetapowo), DUTCH_DESCENDING (3)
- 16 typów oświadczeń w 4 fazach: TOŻSAMOŚĆ → KWALIFIKACJA → ZDOLNOŚĆ → ZOBOWIĄZANIE
- Graf zależności DAG z kaskadowym odrzuceniem (reachability index)
- Decyzja o przystąpieniu: PARTICIPATION_GRANTED / REJECTED / PENDING
- REST API: 7 endpointów, integracja z hexabid-core, hexabid-rules, hexabid-pricing
- Link: [[decisions/2026-05-09-statement-collection-system]]
- Tagi: #statements #dag #auction-types #templates #cascade-rejection #participation-decision

## [2026-05-10] [DESIGN] Alternatywna propozycja systemu zbierania oświadczeń
- Dodano osobny dokument AsciiDoc z propozycją Codex: `ai/wiki/decisions/2026-05-10-statement-collection-system-codex-proposal.adoc`
- Kluczowa decyzja: rozdzielić `AuctionFormat` od `ParticipationPolicyTemplate`, aby mechanika aukcji nie wymuszała jednego zestawu compliance
- Zaproponowano moduł `hexabid-statements`, graf DAG z warunkowymi krawędziami, reachability index, decyzje `ADMITTED`, `REJECTED`, `PENDING`, `ADMITTED_WITH_CONDITIONS`
- Rozszerzono katalog oświadczeń o AML/sankcje, beneficjenta rzeczywistego, konflikt interesów, zmowę, źródło środków, licencje sektorowe, eksport, data room i zdolność środowiskową
- Opisano pięć typów polityk aukcyjnych, natychmiastowe odrzucenie po odpowiedzi dyskwalifikującej oraz integrację z `hexabid-rules`
- Link: [[decisions/2026-05-10-statement-collection-system-codex-proposal]]
- Tagi: #statements #policy-template #graphs #rules #participation-decision

## [2026-05-10] [DESIGN] Weryfikacja systemu oświadczeń z archetypami oprogramowania
- Przejrzano lokalne materiały `.local/archetypyoprogramowania/`, transkrypcje `.local/archetypyoprogramowania/txt/` oraz źródła `/work/projects/github.com/archetypy-oprogramowania/archetypes/`
- Doprecyzowano, że graf jest mechanizmem planowania, ścieżki użytkownika i wyjaśniania wpływu, a nie całym modelem domenowym
- Wprowadzono korekty projektowe: `ParticipationApplication`, `StatementProgramGraph`, `StatementScope`, `StatementExecutionDelta`, `RejectionImpactZone`
- Uzasadniono rozdzielenie `StatementDefinition`, `ParticipationPolicyTemplate` i `StatementProgramInstance` przez analogie do archetypów Graphs, Party, Rules, Plan-vs-Execution, Ordering i Pricing
- Link: [[decisions/2026-05-10-statement-collection-system-codex-proposal]]
- Tagi: #statements #archetypes #graphs #party #rules #plan-vs-execution

## [2026-05-10] [DESIGN] Kategorie aukcji, wymogi i pakiety kwalifikacyjne
- Rozszerzono projekt o `QualificationProfile`: nazwany, wersjonowany pakiet wymagań dla kategorii, jurysdykcji, wartości i ryzyka aukcji
- Rozdzielono `StatementRequirement` od `VerifiedFactRequirement`, `EvidenceRequirement` i `ExternalCheckRequirement`
- Opisano przykłady profili: grunt w Polsce, lek/substancja kontrolowana, alkohol oraz udział w imieniu innego `PartyId`
- Dodano model odpowiedzi z referencjami do stron: `PartyReference`, role reprezentowanego kupującego, beneficjenta, płatnika, odbiorcy, posiadacza licencji i specjalisty medycznego
- Link: [[decisions/2026-05-10-statement-collection-system-codex-proposal]]
- Tagi: #qualification-profile #statements #party #requirements #category-policy

## [2026-05-10] [IMPLEMENTATION] Moduł hexabid-statements — pełna implementacja z poprawkami
- Zaimplementowano moduł domenowy `hexabid-statements` (bez Springa, bez JPA) z modelami: `StatementDefinition`, `StatementCode`, `StatementProgramInstance`, `ParticipationDecision`, `StatementDependencyGraph`, `ParticipationPolicyEvaluator`, `PolicyTemplateCatalog`
- Trzy szablony polityki: `PUBLIC_CONSUMER_LIGHT_V1` (4 oświadczenia), `REGULATED_ASSET_BUYER_V1` (8 oświadczeń), `HIGH_VALUE_TENDER_V1` (11 oświadczeń)
- Graf DAG z sortowaniem topologicznym, wykrywaniem cykli, reachability, dostępnością oświadczeń i kaskadowym odrzuceniem
- 4 porty wejściowe: `StartStatementProgramUseCase`, `SubmitStatementAnswerUseCase`, `GetStatementProgramUseCase`, `GetParticipationDecisionUseCase`
- REST adapter: `RestParticipationApiDelegate` z 4 endpointami Participation API
- JPA adapter: `JpaStatementProgramInstanceRepositoryAdapter` z persystencją programów, odpowiedzi i decyzji
- Naprawiono krytyczne bugi utraty danych w JPA adapterze: persystencja `violationType`, `decidedAt`, `blockedByPrerequisites`; czyszczenie answers przed save; `saveAndFlush` zamiast `save`; zastąpienie `default -> null` wyjątkiem
- Naprawiono `StatementProgramInstance.markCompleted()` — prawidłowa obsługa `Pending` (pozostaje IN_PROGRESS) z użyciem `switch` zamiast `if-else`
- Dodano defensywne kopie map sąsiedztwa w `StatementDependencyGraph`
- Dodano `@Nullable` (jspecify) do view DTOs: `StatementStepView.answerValue`, `StatementProgramView.decision`, `ParticipationDecisionView.rootCause`, `ParticipationDecisionView.humanReason`
- Dodano JavaDoc do wszystkich publicznych typów w module hexabid-statements (48 plików źródłowych)
- Dodano JavaDoc do REST i DB adapterów
- Testy jednostkowe: 14 pass (8 graph + 2 definition + 4 evaluator)
- Testy integracyjne: 10 scenariuszy (SC1-SC10) w `StatementCollectionFlowIT`
- Weryfikacja E2E: SC1 (admitted) i SC2 (rejected with cascade) potwierdzone curl-em
- Frontend: wygenerowany ParticipationApi.ts z OpenAPI contract, `npm run build` pass
- Architektura: `CoreArchitectureTest` sprawdza brak zależności `hexabid-statements` od Spring/JPA
- Link: [[decisions/2026-05-10-statement-collection-system-codex-proposal]]
- Tagi: #statements #implementation #javadoc #bugfix #integration-tests #participation-decision

## [2026-05-10] [DESIGN] Projekt UX oświadczeń i szablonów kwalifikacji w Hexabid SPA
- Kompleksowa analiza obecnego stanu frontend (ParticipationApi wygenerowany ale nieużywany, płaski formularz tworzenia aukcji, brak ścieżki kwalifikacji licytanta)
- Projekt wizarda tworzenia aukcji: 4 kroki (Podstawy → Format i kwalifikacja → Konfiguracja ceny → Podsumowanie) zamiast płaskiego formularza
- Projekt ParticipationGate: bramka kwalifikacji na stronie szczegółów aukcji, 5 stanów (NOT_STARTED / IN_PROGRESS / ADMITTED / REJECTED / PENDING)
- Projekt StatementWizard: DAG-driven UI sterowany z backendu, grupowanie oświadczeń po krokach, ostrzeżenia przy odpowiedziach dyskwalifikujących, ekran odrzucenia z kaskadą
- Nowe komponenty: ParticipationGateComponent, StatementWizardComponent, StatementStepCardComponent, RejectionScreenComponent, FormatAdmissionStepComponent
- Nowy data-access layer: participation-api.models.ts, participation-view.mapper.ts, participation-api.service.ts
- Integracja: ParticipationGate na AuctionDetailsPage, Rules panel linkuje do kwalifikacji, bid panel warunkowy na podstawie decision status
- Wymagane zmiany backend: dodanie auctionFormat i participationPolicyTemplate do CreateAuctionRequest, dodanie participationPolicyTemplate do AuctionResponse
- Plan implementacji: 5 faz (data-access → participation → wizard tworzenia → backend API → integracja i polish)
- Link: [[decisions/2026-05-10-statements-ux-design]]
- Tagi: #statements #ux #wizard #participation #frontend #angular #dag-driven-ui

## [2026-05-11] [DESIGN] Propozycja Codex dla UI/UX kwalifikacji i oświadczeń w SPA
- Dodano osobny dokument AsciiDoc `ai/wiki/decisions/2026-05-11-statements-ui-ux-codex-proposal.adoc`
- Zweryfikowano aktualny frontend: `ParticipationApi` jest wygenerowany, `/sell` pozostaje płaskim formularzem, a `AuctionResponse`/`CreateAuctionRequest` nie niosą profilu kwalifikacyjnego aukcji
- Zaproponowano `Auction Setup Studio` dla sprzedającego i `Participation Center` dla licytanta jako procesy nadrzędne wobec prostego statement wizard
- Wprowadzono docelowy model UI `QualificationTaskVm`, który rozróżnia `STATEMENT`, `VERIFIED_FACT`, `EVIDENCE`, `EXTERNAL_CHECK` i `PARTY_REFERENCE`
- Opisano plan wdrożenia w 5 fazach: most do obecnego backendu, blokowanie licytacji decyzją, kreator aukcji MVP, kontrakt profili kwalifikacyjnych, pełny UX wymogów
- Link: [[decisions/2026-05-11-statements-ui-ux-codex-proposal]]
- Tagi: #statements #ux #frontend #qualification-profile #participation-center #auction-setup

## [2026-05-11] [IMPLEMENTATION] Faza 1 Participation Center — most do backendu oświadczeń
- Zaimplementowano Fazę 1 planu z `ai/wiki/decisions/2026-05-11-statements-ui-ux-codex-proposal.adoc`
- Nowe pliki data-access: `participation-api.models.ts` (QualificationTaskVm, QualificationProgramVm, ParticipationDecisionVm, SubmitAnswerResultVm), `participation-view.mapper.ts` (mapowanie StatementProgramView → QualificationProgramVm), `participation-api.service.ts` (ParticipationApiService wrapping generated ParticipationApi)
- Nowy feature: `features/participation/` z `ParticipationFacade`, `ParticipationCenterComponent`, `QualificationTaskCardComponent`
- Integracja ze stroną aukcji: ParticipationCenterComponent nad panelem licytacji, bidDisabledReason() uwzględnia ParticipationDecision
- Centrum dopuszczenia: status programu, pasek postępu, mapa zadań, formularze odpowiedzi (YES_NO, TEXT), ostrzeżenie przy odpowiedziach dyskwalifikujących, panel decyzji
- E2E: `participation.spec.ts` — 5 testów (centrum widoczne, start programu, zadania, blokada licytacji, odpowiedzi pozytywne)
- `npm run build` pass
- Link: [[decisions/2026-05-11-statements-ui-ux-codex-proposal]]
- Tagi: #statements #participation-center #spa #angular #e2e #qualification-task

## [2026-05-11] [IMPLEMENTATION] Faza 2 — bezpieczne blokowanie licytacji przez ParticipationDecision
- Wydzielono `AuctionBidPanelComponent` z formularzem oferty i logiką blokowania
- Panel ma 5 trybów: `bid` (formularz), `qualification-needed` (CTA do ParticipationCenter), `rejected` (odmowa bez formularza), `seller` (info), `inactive` (aukcja nieaktywna)
- Gdy `ParticipationDecision` = REJECTED — formularz licytacji jest całkowicie ukryty, widoczny komunikat odrzucenia z przyczyną
- Gdy brak decyzji lub status inny niż ADMITTED — widoczny CTA "Rozpocznij dopuszczenie w Centrum dopuszczenia powyżej"
- Gdy ADMITTED_WITH_CONDITIONS — widoczny formularz z ostrzeżeniem o warunkach
- Rules panel zachowany jako dodatkowy warunek (hasBiddingBlocks nadal blokuje submit)
- E2E rozszerzone o testy Fazy 2: qualification CTA, rejected message bez formularza, bid form unlock
- `npm run build` pass
- Link: [[decisions/2026-05-11-statements-ui-ux-codex-proposal]]
- Tagi: #statements #bid-panel #participation-decision #spa #angular #e2e

## [2026-05-11] [IMPLEMENTATION] Faza 3 — Kreator aukcji MVP (Auction Setup Studio)
- Zastąpiono płaski formularz `/sell` kreatorem krokowym `AuctionSetupPageComponent` w `features/setup/`
- 4 kroki: Przedmiot i kategoria → Kwalifikacja licytantów → Cena i zabezpieczenia → Podsumowanie
- Krok 1: wybór kategorii (8 typów), jurysdykcji, tytuł, cena, termin; panel "Wykryte wymagania"
- Krok 2: wybór profilu kwalifikacyjnego z tymczasowego katalogu (3 profile: PUBLIC_CONSUMER_LIGHT_V1, REGULATED_ASSET_BUYER_V1, HIGH_VALUE_TENDER_V1); podgląd ścieżki licytanta
- Krok 3: konfiguracja PricingConfig (wadium, VAT, akcyza, cło) — przeniesione z dawnego `/sell`
- Krok 4: podsumowanie z pełnym przeglądem konfiguracji, ścieżką licytanta, informacją o profilu eksperymentalnym
- Nawigacja: step tabs (klikalne), przyciski Wstecz/Dalej/Zapisz szkic
- Nowy plik: `data-access/contracts/qualification-profile.models.ts` z katalogiem profili, labelami, funkcjami `profilesForCategory()`, `recommendedProfile()`
- Route `/sell` kieruje do `AuctionSetupPageComponent`; stary `AuctionCreatePageComponent` zachowany w `features/create/`
- E2E: `e2e/auction-setup.spec.ts` — 8 testów (ładowanie, kategoria, profile, bidder preview, pricing, review, nawigacja, pełen flow z logowaniem)
- `npm run build` pass
- Link: [[decisions/2026-05-11-statements-ui-ux-codex-proposal]]
- Tagi: #auction-setup #wizard #qualification-profile #spa #angular #e2e #sell

## [2026-05-12] [IMPLEMENTATION] Faza 4 — Kontrakt profili kwalifikacyjnych i automatyczne dopasowanie
- Zaimplementowano Fazę 4 planu z `ai/wiki/decisions/2026-05-11-statements-ui-ux-codex-proposal.adoc`
- **Backend: `RestQualificationApiDelegate`** — implementacja `GET /api/qualification-profiles`, zwraca katalog z `PolicyTemplateCatalog` (3 profile z labelami, opisami, taskCount, estimatedMinutes, abandonmentRisk, recommended)
- **Backend: `RestParticipationApiDelegate`** — gdy `templateName` nie jest przekazany w `StartParticipationProgramRequest`, delegat rozwiązuje profil z aukcji przez `FindAuctionDetailsUseCase`; licytant nie musi wybierać szablonu
- **Backend: `RestAuctionContractMapper`** — dodano `templateLabel` do `AuctionQualificationSummary` w odpowiedzi
- **Frontend: `qualification-profile-api.service.ts`** — nowy serwis opakowujący wygenerowany `QualificationApi`
- **Frontend: `qualification-profile.models.ts`** — zaktualizowano katalog profili (zgodny z backendowymi `StatementCode` zamiast fikcyjnych kodów), dodano `profileByTemplateName()` i `mapApiRiskToRisk()`
- **Frontend: `auction-api.models.ts`** — dodano `QualificationSummaryVm` i pole `qualificationSummary` w `AuctionDetailsVm`
- **Frontend: `auction-view.mapper.ts`** — mapowanie `AuctionResponse.qualificationSummary` do `QualificationSummaryVm`
- **Frontend: `AuctionSetupPageComponent`** — ładuje profile z API w `constructor()`, wysyła `participationPolicyTemplate` przy tworzeniu aukcji; usunięto notatkę eksperymentalną, zastąpiono komunikatem o automatycznym przypisaniu profilu
- **Frontend: `ParticipationApiService`** — `startProgram()` akceptuje opcjonalny `templateName` (gdy brak, backend używa profilu aukcji)
- **Frontend: `ParticipationFacade`** — `startProgram()` akceptuje opcjonalny `templateName`
- **Frontend: `ParticipationCenterComponent`** — przyjmuje input `[qualificationSummary]`, pokazuje label profilu i taskCount przed rozpoczęciem programu, nie hardcoduje już szablonu
- **Frontend: `auction-details-page.component.html`** — przekazuje `[qualificationSummary]="auction.qualificationSummary"` do participation center
- E2E: `e2e/qualification-profile.spec.ts` — 5 testów (API catalog, tworzenie aukcji z profilem, start programu bez templateName, SPA profile assignment note, participation center z qualification summary)
- `mvn clean verify -Plocal -DskipTests` pass, `npm run build` pass
- Link: [[decisions/2026-05-11-statements-ui-ux-codex-proposal]]
- Tagi: #qualification-profile #api-contract #participation #spa #angular #e2e #phase4

## [2026-05-12] [IMPLEMENTATION] Faza 5 — Pełne UX wymogów: task kinds, stages, PartyReference, Moje dopuszczenia
- Zaimplementowano Fazę 5 planu z `ai/wiki/decisions/2026-05-11-statements-ui-ux-codex-proposal.adoc`
- **Mapper: inferowanie task kind ze statement code** — `participation-view.mapper.ts` mapuje kody oświadczeń na `QualificationTaskKind`: PARTY_REFERENCE (LEGAL_CAPACITY, BENEFICIAL_OWNER), EXTERNAL_CHECK (SANCTIONS, PEP, AML), EVIDENCE (SECTOR_LICENSE, PERMIT, EXPORT_CONTROL), VERIFIED_FACT (ADULT, AGE, KYC), STATEMENT (reszta)
- **Mapper: kindLabel/kindDescription** — eksportowane funkcje label i opisu dla każdego task kind
- **QualificationTaskCardComponent** — nowy badge `task-kind-badge` (Oświadczenie/Wymóg weryfikacji/Wymagany dokument/Sprawdzenie zewnętrzne/Identyfikacja podmiotu), kolorowe border-left per kind (niebieski=VERIFIED_FACT, żółty=EVIDENCE, fioletowy=EXTERNAL_CHECK, zielony=PARTY_REFERENCE), opis hint per kind, dynamiczny action label (Potwierdź/Dołącz dokument/Zgódź się na weryfikację/Wskaż podmiot)
- **ParticipationCenterComponent** — grupowanie zadań po `stepLabel` w sekcje `stage-group` z nagłówkiem i liczbą ukończonych; pasek postępu z procentem; interfejs `StageGroup`
- **PartyReferencePickerComponent** — nowy komponent z dwiema opcjami: "Działam we własnym imieniu" (SELF) i "Działam w imieniu innego podmiotu" (REPRESENTATIVE)
- **MyParticipationsPageComponent** — nowa strona `/me/participations` z `MyParticipationsFacade`; ładuje aukcje z `browseMyBids` i dla każdej pobiera program dopuszczenia; pokazuje karty z statusem, postępem, powodem odrzucenia
- **Nawigacja** — dodano "Moje dopuszczenia" do topbar nav; sekcja w `/dashboard` z linkiem do `/me/participations`; nowa ruta w `app.routes.ts`
- **E2E: `qualification-ux.spec.ts`** — 6 testów: task kind badges, stage grouping, LAND category regulated profile, ALCOHOL regulated tasks, my participations page, party reference picker
- `mvn clean verify -Plocal -DskipTests` pass, `npm run build` pass
- Link: [[decisions/2026-05-11-statements-ui-ux-codex-proposal]]
- Tagi: #phase5 #task-kind #stages #party-reference #my-participations #spa #angular #e2e

## [2026-05-15] [DESIGN] Finalny model autoryzacji RBAC + Scope przez authorized query
- Uporzadkowano wiki autoryzacji: zostawiono jedna finalna wersje ADR i jedna finalna wersje planu, usunieto poprzednie warianty `OrganizationPath`, prefix w `PartyId` oraz JWT-only assignments
- Przyjeto rekomendacje: JWT nie jest zrodlem pelnej decyzji zasobowej; niesie tylko `sub`, `roles`, `organisationCode`
- Role mapowane sa w aplikacji na `Permission(ResourceType, Action, Relation)`, aby uniknac eksplozji plaskich permissionow
- Zasob aukcyjny ma przechowywac `created_by_user_id` i `created_by_organisation_code`
- Relacja manager-podwladny pozostaje w DB w tabeli `user_supervision`
- Centralnym mechanizmem jest authorized query: zasob pobierany jest z DB razem z warunkiem autoryzacyjnym
- `organisationCode` jest kanonicznym path code porownywanym przez `equals` albo `LIKE code/%`, z kontrola granicy segmentu
- ADR: [[decisions/2026-05-15-authorization-rbac-scope-final]]
- Plan: [[plans/authorization-rbac-scope-final]]
- Tagi: #authorization #rbac #scope #jwt #organisation-code #authorized-query #final

## [2026-06-07] [UPGRADE] Angular 22 upgrade for hexabid-spa
- Upgraded Angular from 20.3.0 to 22.0.0 with TypeScript 6.0.3
- Updated all @angular/* packages to 22.0.0 in hexabid-spa/package.json
- Updated devDependencies: @angular/build, @angular/cli, @angular/compiler-cli to 22.0.0
- Required Node.js >=24.15.0 (installed via nvm)
- All 53 Playwright e2e tests pass
- Angular dev server runs successfully on port 14200
- Build warnings: NG8112 (unused @let), NG8113 (unused RouterLink), NG8107 (optional chain simplification) - non-breaking
- No @angular/mcp package exists; module communication uses standard Angular patterns
- Updated AGENTS.md stack techniczny to Angular 22
- Tag: #angular22 #upgrade #frontend #typescript6

## [2026-06-26] [BUGFIX] Quantity.subtract() explicit negative guard
- Added pre-condition check in `Quantity.subtract()` with clear error message when result would be negative
- Previously relied on constructor validation which threw misleading error
- Added test `shouldRejectSubtractWhenResultWouldBeNegative`
- Tagi: #bugfix #quantity #p1

## [2026-06-26] [REFACTOR] payment-core: Spring removal + Faza 1.2 model enrichment
- **Faza 1.1**: Removed Spring from hexabid-payment-core:
  - Removed `spring-context` dependency from pom.xml
  - Removed `PaymentGatewayRegistry` and `AuctionWonEventListener` from payment-core
  - Moved event listener to hexabid-bootstrap as `PaymentEventSubscriber` `@Component`
  - Refactored `RestPaymentApiDelegate` to inject `PaymentGatewayDiscoverer` list directly
  - Refactored `PaymentConfiguration` (removed registry bean, kept gateway + use case beans)
- **Faza 1.2**: Enriched payment-core accounting model:
  - Added `AccountType` enum (ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE)
  - Added `AccountType` field to `Account` record
  - Added `ChartOfAccounts` — registry with find/register/lookup methods
  - Added `Ledger` — post transactions, compute balances, query by account
  - Added `Payment` aggregate — lifecycle (PENDING→COMPLETED/FAILED→REFUNDED) with state machine
  - Added `PaymentId` value object
- Tagi: #refactor #payment-core #hexagonal-architecture #spring-removal #accounting #ledger #payment-aggregate

## [2026-06-26] [IMPLEMENTATION] Faza 2 — hexabid-inventory: Availability, Reservation, InventoryMovement
- Added `Availability` value object (total/reserved/available breakdown)
- Added `Reservation` aggregate with state machine (ACTIVE→CONFIRMED/CANCELLED/EXPIRED)
- Added `ReservationId` and `AuctionId` value objects (inventory-scoped, no dependency on hexabid-core)
- Added `ReservationService` — reserve/confirm/cancel with availability guard
- Added `InventoryMovement` — track movements with type (RECEIPT, SALE, RETURN, TRANSFER, ADJUSTMENT, LOSS)
- 8 new unit tests (reserve, availability, confirm, cancel, free availability, filter by auction, reject oversell, record movement)
- Total inventory tests: 17 pass
- Tagi: #inventory #availability #reservation #movement #faza2

## [2026-06-26] [BUGFIX] hexabid-pricing — createExciseCalculator ignores ExciseType (PER_UNIT vs PERCENTAGE)
- **Bug**: `AuctionPricingFacade.createExciseCalculator()` always created `PercentageCalculator`, even for `ExciseType.PER_UNIT`. The component tree path (`calculateWithComponentTree()`) produced wrong results for per-unit excise.
- **Fix**: `createExciseCalculator` now switches on `ExciseType` — `PercentageCalculator` for PERCENTAGE, `PerUnitCalculator` for PER_UNIT.
- **Enhancement**: Added `withFixedParam()` to `SimplePriceComponent.Builder` to pass non-Money parameters (like `quantity`) to calculators in the component tree.
- **Test**: Added `shouldHandlePerUnitExciseInComponentTree` test to `ComponentTreeTests`.
- Tagi: #pricing #bugfix #excise #component-tree #per-unit

## [2026-06-26] [IMPLEMENTATION] Faza 4 — hexabid-core: EditAuctionService implementation
- Implemented `EditAuctionService` implementing the existing `EditAuctionUseCase` port
- Supports editing DRAFT auctions (title + startingPrice) via `Auction.edit()` model method
- Returns `AuctionNotFound` / `EditNotAllowed` / `AuctionEdited` results
- 3 new unit tests: edit draft, reject not-found, reject non-draft status
- Party enrichment and Order aggregate deferred (P3 extensions)
- Tagi: #core #auction-edit #usecase #faza4

## [2026-06-26] [IMPLEMENTATION] Faza 5 — hexabid-statements: domain events + event publisher integration
- Added sealed `StatementDomainEvent` interface with 4 events: `ProgramStartedEvent`, `AnswerSubmittedEvent`, `ProgramCompletedEvent`, `ProgramRejectedEvent`
- Added `StatementEventPublisher` output port (follows core's `AuctionEventPublisher` pattern)
- Integrated `eventPublisher.publish()` calls into `StatementService` at all lifecycle points:
  - `startProgram()` → publishes `ProgramStartedEvent`
  - `submitAnswer()` → publishes `AnswerSubmittedEvent` for each answer, plus `ProgramRejectedEvent` on disqualification or `ProgramCompletedEvent` upon completion
- Backward-compatible: existing 2-arg constructor delegates to no-op publisher; all 14 existing tests pass unchanged
- Full test suite: 31 modules, all tests pass (incl. ArchUnit architecture tests)
- Tagi: #statements #domain-events #hexagonal-architecture #event-publisher #faza5

## [2026-06-27] [FIX] LocalSecurityConfiguration — Dev OAuth2 login broken (ERR_INVALID_AUTH_CREDENTIALS)
- **Root cause**: `LocalSecurityConfiguration` in `hexabid-adapter-in-auth-local` had two violations per AGENTS.md:
  - `SessionCreationPolicy.STATELESS` — breaks HTTP session required by formLogin and OAuth2 login
  - `httpBasic(Customizer.withDefaults())` — sends `WWW-Authenticate: Basic`, blocks OAuth2 redirect (Chrome: `ERR_INVALID_AUTH_CREDENTIALS`)
  - Missing `formLogin`, `oauth2Login`, `exceptionHandling`, and `oauth2Client`
- **Fix**: Replaced with proper configuration matching `OAuth2SecurityConfiguration` pattern:
  - Removed `SessionCreationPolicy.STATELESS` and `httpBasic`
  - Added `formLogin` (implicit via `.oauth2Login().loginPage("/login/dev")`), `oauth2Login`, `oauth2Client`, `logout`
  - Added `exceptionHandling` with `HttpStatusEntryPoint(UNAUTHORIZED)` for API 401
  - Added `permitAll()` for `/login/**`, `/logout`, `/dev-auth/**`, `/ws-auctions/**`
- **Why it happened**: `OAuth2SecurityConfiguration` uses `@ConditionalOnMissingClass("...LocalSecurityConfiguration")`, so it was completely skipped when the local auth module was on classpath. `LocalSecurityConfiguration` was the only active filter chain, but it had a broken config that blocked OAuth2.
- **Verification**: Dev login flow works end-to-end — click Dev → user selection page → redirect to SPA as logged-in user ("online" session status). All console errors resolved (only pre-existing a11y warnings).
- Tagi: #fix #auth #oauth2 #local-security #httpbasic #session

## [2026-06-27] [GIT] Rebase quality-update na main
- `quality-update` został zresetowany do `main`, następnie cherry-pick 3 commitów jakościowych (bez auth dev commits):
  - `d7dd80bf` feat: upgrade hexabid-spa to Angular 22 with TypeScript 6.0.3
  - `f6dad899` angular 22
  - `552f787f` quality upgrade (refactor 6 modułów + LocalSecurityConfiguration fix)
- **Pominięto**: 9 auth commitów (raw dev wersje), które są w main w postaci squashed (`authorization (#23)` + `Authorization (#24)`)
- **Weryfikacja**: `mvn clean install -Plocal -DskipTests` → 31 modułów OK; `mvn test` → wszystkie testy OK; SPA działa poprawnie z zalogowanym użytkownikiem
- Tagi: #git #rebase #quality-update #cherry-pick

## [2026-06-27] [DOCUMENTATION] Documentation cleanup — deleted obsolete plans, updated plan status, wiki refresh
- **Usunięto nieaktualne pliki**: `doc/domain-modeling/domain-modeling-plan.md`, `doc/reports/*` (4 pliki), `doc/specyfikacja-rozwojowa-hexabid.md`, `doc/localhost.har`, `ai/wiki/.plan-system-regul.adoc.kate-swp`
- **Zaktualizowano** `doc/plan-archetypow-update.adoc`:
  - Statusy w Plan fazowy: Faza 0 ✓, Faza 1 ✓ częściowo, Faza 2 ✓ częściowo, Faza 3 — deferowane, Faza 4 ✓ częściowo
  - Payment-core ocena: SŁABA → DOBRA po przebudowie
  - Inventory ocena: SŁABA → ŚREDNIA po wzbogaceniu
  - Wszystkie P1/P2/P3 naprawione oznaczone `NAPRAWIONY` / `[green]#✓#`
  - Zaktualizowane wskaźniki jakości (realne liczby zamiast aspirujących)
- **Zaktualizowano dokumenty wiki** dla agentów:
  - `ai/wiki/index.md`: Ostatnie zmiany → docelowa sekcja z listą zrealizowanych inicjatyw
  - `ai/wiki/plans/authorization-rbac-scope-final.md` → oznaczony jako IMPLEMENTED
  - `AGENTS.md` odświeżony kontekst
- Tagi: #documentation #cleanup #obsolete-files #plan-update #wiki #agent-docs
