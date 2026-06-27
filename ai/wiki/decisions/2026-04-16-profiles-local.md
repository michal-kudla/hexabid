# ADR: System Profili Maven/Spring

## TLDR
Wprowadzono spójny system profili z prefiksem `local-` dla komponentów development, unikając konfliktu z środowiskami `dev/prod/uat`.

## Tagi
#configuration #profiles #maven #spring #development

## Kontekst
Projekt potrzebował systemu profili dla różnych środowisk i konfiguracji:
- Maven profiles dla wyboru adapterów (auth, KYC, payment)
- Spring profiles dla konfiguracji środowiska (port, seed data)
- Konflikt nazewnictwa: `dev` używane zarówno jako profil Maven jak środowisko

## Decyzja
1. **Profil Maven główny**: `local` (aktywny domyślnie)
   - Łączy: `local-auth`, `local-kyc`, `local-payment`
2. **Spójne nazewnictwo**: Wszystkie development komponenty z prefiksem `local-`
3. **Profil Spring**: `local` z konfiguracją development
   - Port: 18080, Context: `/hexabid`, Seed data: włączone

## Alternatywy Rozważane
- **Opcja A**: Zachować `dev` dla Maven, używać `development` dla Spring
- **Opcja B**: Używać tylko Spring profiles, zrezygnować z Maven profiles
- **Opcja C**: Kompletne przeprojektowanie systemu profili

**Wybrano obecną opcję** ponieważ:
- Rozdziela odpowiedzialności Maven/Spring
- Unika konfliktów nazewnictwa
- Jest intuicyjna dla developerów

## Konsekwencje

### Plusy
- ✅ Spójne nazewnictwo bez konfliktów
- ✅ Intuicyjne dla developerów (`local` = development)
- ✅ Łatwe uruchomienie: `mvn clean install` + `--spring.profiles.active=local`
- ✅ Możliwość rozszerzenia o inne środowiska

### Minusy
- ❌ Dodatkowa złożoność systemu profili
- ❌ Konieczność zrozumienia różnicy Maven vs Spring profiles

## Implementacja
1. Zaktualizowano `hexabid-bootstrap/pom.xml` z nowymi profilami
2. Skonfigurowano `application-local.yaml`
3. Przetestowano kompilację i uruchomienie
4. Zaktualizowano dokumentację

## Status
✅ **Zakończone** - System profili działa poprawnie.

## Linki
- [[PROFIL_LOCAL_GUIDE]] - Szczegółowy przewodnik
- [[log]] - Szczegóły implementacji
