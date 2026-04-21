# LLM WIKI - Index

## Witaj w LLM WIKI dla projektu Hexabid

Ta wiki stanowi **źródło prawdy** dla wszystkich decyzji architektonicznych, konfiguracji i wiedzy projektowej. Jest zarządzana przez agentów AI i aktualizowana przy każdej istotnej zmianie.

## 📚 Struktura Wiki

### 🏗️ Architektura i Decyzje
- [[decisions/2026-04-16-module-refactoring]] - Refaktoryzacja modułów i pakietów
- [[decisions/2026-04-16-profiles-local]] - System profili Maven/Spring
- [[decisions/2026-04-16-documentation-structure]] - Organizacja dokumentacji
- [[decisions/2026-04-17-pricing-architecture]] - Architektura ceny (wadium, VAT, akcyza, cło) oparta na archetypach M03+M04

### 📋 Koncepcje i Definicje
- [[concepts/hexagonal-architecture]] - Architektura heksagonalna w Hexabid
- [[concepts/profile-system]] - System profili Maven i Spring
- [[concepts/package-migration]] - Migracja pakietów z com.acme.auctions

### 🔧 Konfiguracje i Setup
- [[PROFIL_LOCAL_GUIDE]] - Przewodnik po profilach lokalnych + systemd user services
- [[DOCUMENTATION_STRUCTURE]] - Organizacja dokumentacji w projekcie

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

*Automatycznie zarządzane przez agentów AI. Ostatnia aktualizacja: 2026-04-17*
