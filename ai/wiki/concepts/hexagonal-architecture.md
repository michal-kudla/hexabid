# Architektura Heksagonalna w Hexabid

## TLDR
Hexabid implementuje czystą architekturę heksagonalną z ścisłym podziałem na domenę, porty i adaptery.

## Tagi
#architecture #hexagonal #domain-driven-design #ports-adapters

## Szczegóły

### Struktura Modułów

**Moduły Domenowe** (czyste Java, bez framework'ów):
- `hexabid-core` - Biznesowa logika aukcji
- `hexabid-auth-core` - Model uwierzytelniania i użytkowników
- `hexabid-payment-core` - Logika płatności
- `hexabid-quantity` - Jednostki i wartości ilościowe
- `hexabid-product` - Katalog produktów

**Porty i Adaptery**:
- **Inbound**: REST API, WebSocket, Job scheduler, OAuth2
- **Outbound**: Database, Kafka, KYC, Payment gateways

### Reguły Architektury

1. **Domeny bez zależności Spring/JPA** - używają `@NullMarked` z jspecify
2. **Separacja kodu generowanego** - `com.github.hexabid.contract.*` vs `com.github.hexabid.adapter.*`
3. **Dependency injection tylko w adapterach** - domeny to proste klasy

### Egzekwowanie
Architektura jest egzekwowana przez `hexabid-architecture-tests` używając ArchUnit.

## Linki
- [[decisions/2026-04-16-module-refactoring]] - Refaktoryzacja pakietów
- [[AGENTS.md]] - Szczegółowe reguły architektury
