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
