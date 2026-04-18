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
