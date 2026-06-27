# ADR: Refaktoryzacja Modułów i Pakietów

## TLDR
Zmieniono nazwy wszystkich modułów z `auctions-*` na `hexabid-*` i pakietów z `com.acme.auctions` na `com.github.hexabid` dla spójności brandingowej.

## Tagi
#architecture #refactoring #modules #packages #branding

## Kontekst
Projekt miał niespójne nazewnictwo modułów:
- Niektóre moduły: `auctions-core`, `auctions-auth-core`
- Inne moduły: `quantity`, `product`, `inventory` (bez prefiksu)
- Pakiety: `com.acme.auctions.*` (stary branding)

## Decyzja
1. **Nazwy modułów**: Wszystkie moduły otrzymują prefiks `hexabid-`
2. **Pakiety**: Migracja na `com.github.hexabid.*`
3. **OpenAPI**: Regeneracja kontraktów z nowymi pakietami
4. **Zgodność wsteczna**: Zachowanie funkcjonalności

## Konsekwencje

### Plusy
- ✅ Spójne nazewnictwo wszystkich modułów
- ✅ Profesjonalny branding z nazwą projektu
- ✅ Łatwiejsze zarządzanie zależnościami
- ✅ Lepiej zorganizowana struktura pakietów

### Minusy
- ❌ Wymaga aktualizacji wszystkich importów
- ❌ Konieczność regeneracji kodu OpenAPI
- ❌ Ryzyko błędów podczas migracji

## Implementacja
1. Zmieniono nazwy katalogów modułów
2. Zaktualizowano `pom.xml` wszystkich modułów
3. Migracja pakietów Java
4. Regeneracja OpenAPI kontraktów
5. Aktualizacja importów w całym kodzie

## Status
✅ **Zakończone** - Wszystkie moduły kompilują się i aplikacja uruchamia się poprawnie.

## Linki
- [[concepts/hexagonal-architecture]] - Architektura projektu
- [[log]] - Szczegóły implementacji
