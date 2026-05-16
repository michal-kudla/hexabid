# LLM WIKI - Index

## Witaj w LLM WIKI dla projektu Hexabid

Ta wiki stanowi **źródło prawdy** dla wszystkich decyzji architektonicznych, konfiguracji i wiedzy projektowej. Jest zarządzana przez agentów AI i aktualizowana przy każdej istotnej zmianie.

## 📚 Struktura Wiki

### 🏗️ Architektura i Decyzje
- [[decisions/2026-04-16-module-refactoring]] - Refaktoryzacja modułów i pakietów
- [[decisions/2026-04-16-profiles-local]] - System profili Maven/Spring
- [[decisions/2026-04-16-documentation-structure]] - Organizacja dokumentacji
- [[decisions/2026-04-17-pricing-architecture]] - Architektura ceny (wadium, VAT, akcyza, cło) oparta na archetypach M03+M04
- [[decisions/2026-04-21-pricing-spa-frontend]] - Frontend SPA modułu pricing: kalkulacja ceny, wadium, konfiguracja
- [[decisions/2026-05-04-spa-e2e-business-flow]] - Reprojekt E2E SPA na scenariusze biznesowe click-through
- [[decisions/2026-05-05-local-port-configuration]] - Ujednolicenie lokalnych adresów backendu i SPA
- [[decisions/2026-05-05-dev-auth-e2e]] - Dev auth jako jawny wybór użytkownika i scenariusz E2E
- [[decisions/2026-05-05-auction-activation-rules-guidance]] - Aktywacja szkicu aukcji oraz rozdzielenie reguł licytacji i rozliczenia w UI
- [[decisions/2026-05-09-statement-collection-system]] - System zbierania oświadczeń: DAG zależności, 4 typy aukcji, 16 oświadczeń, kaskadowe odrzucenie
- [[decisions/2026-05-10-statement-collection-system-codex-proposal]] - Alternatywna propozycja Codex: rozdzielenie formatu aukcji od wersjonowanego szablonu polityki dopuszczenia, profile kwalifikacyjne kategorii, zweryfikowane z archetypami Graphs/Party/Rules/Plan-vs-Execution
- [[decisions/2026-05-10-statements-ux-design]] - Projekt UX oświadczeń i szablonów kwalifikacji w SPA: wizard tworzenia aukcji, bramka kwalifikacji licytanta, DAG-driven statement wizard, integracja z rules panel
- [[decisions/2026-05-11-statements-ui-ux-codex-proposal]] - Propozycja Codex dla UI/UX kwalifikacji: Auction Setup Studio, Participation Center, QualificationTask VM, rozdzielenie oświadczeń od wymogów, dowodów i weryfikacji
- [[decisions/2026-05-15-authorization-rbac-scope-final]] - **AKTUALNY ADR**: Autoryzacja RBAC + Scope przez `JWT roles + organisationCode`, permission model bez eksplozji i authorized query w DB

### 📋 Koncepcje i Definicje
- [[concepts/hexagonal-architecture]] - Architektura heksagonalna w Hexabid
- [[concepts/profile-system]] - System profili Maven i Spring
- [[concepts/package-migration]] - Migracja pakietów z com.acme.auctions
- [[concepts/pricing-spa-layers]] - Warstwy frontendowe modułu pricing (data-access, feature, e2e)

### 📋 Konfiguracje i Setup
- [[PROFIL_LOCAL_GUIDE]] - Przewodnik po profilach lokalnych + systemd user services
- [[DOCUMENTATION_STRUCTURE]] - Organizacja dokumentacji w projekcie

### 📐 Plany wdrożeniowe
- [[plans/authorization-rbac-scope-final]] - **AKTUALNY PLAN**: Implementacja autoryzacji przez `PrincipalContext`, `Permission(ResourceType, Action, Relation)` i authorized queries

## 📝 Procedura Pielęgnacji

Po zakończeniu każdej istotnej sesji agent wykonuje:

1. **Analiza sesji** - Wyodrębnienie kluczowych decyzji i zmian
2. **Aktualizacja tego indexu** - Dodanie nowych linków z krótkim opisem
3. **Logowanie w [[log]]** - Zapisanie wpisu w formacie `## [DATA] [TYP] Opis zmiany`
4. **Cross-referencing** - Linkowanie powiązanych dokumentów

## 🎯 Zasady

- **Source of Truth**: Każda decyzja trafia do wiki
- **Format**: `Tytuł`, `TLDR`, `Tagi`, `Treść`
- **Linki**: Używaj formatu `[[nazwa-pliku.md]]`
- **Aktualność**: Wiki jest zawsze aktualna

---

*Automatycznie zarządzane przez agentów AI. Ostatnia aktualizacja: 2026-05-15*
