# Hexabid - AI Agent Guidelines

Jesteś asystentem AI pracującym nad projektem **Hexabid**. Zanim rozpoczniesz jakiekolwiek modyfikacje kodu, musisz zapoznać się z poniższymi zasadami i bezwzględnie ich przestrzegać. Celem projektu jest **wzorcowe ukazanie najwyższej jakości praktyk inżynieryjnych**.

## Główne założenia Architektoniczne

1. **Architektura Heksagonalna (Porty i Adaptery)**:
   - Logika biznesowa (`auctions-core`, `auctions-payment-core`, `auctions-auth-core`) musi być całkowicie niezależna od frameworków (Spring) i bazy danych (JPA, Hibernate). Używamy czystego kodu w Javie.
   - Komunikacja ze światem zewnętrznym odbywa się WYŁĄCZNIE przez Porty (interfejsy).
   - Adaptery (wewnętrzne: REST, WS, JOB; zewnętrzne: DB, KAFKA, KYC, API) implementują Porty.
   - Każdy adapter jest w osobnym module Mavena.

2. **Domain-Driven Design (DDD) i Archetypy Biznesowe**:
   - Modelowanie opiera się na wzorcach i archetypach (np. Peter Coad's archetypes: `Party`, `Product`, `Lot`, `Accounting`).
   - "Krzycząca domena" (Screaming Architecture) - struktura pakietów w module `core` ma odzwierciedlać procesy biznesowe (np. `auctioning`, `party`, `lot`), a nie techniczne wzorce (nie używamy pakietów w stylu `services`, `controllers` w warstwie domeny).

3. **Contract-First (OpenAPI)**:
   - Definicje API (REST) powstają najpierw w plikach YAML (`auctions-api-contract/src/main/resources/openapi/`).
   - Kod Java (interfejsy i DTOs) oraz TypeScript (dla SPA) jest generowany automatycznie przy budowaniu modułu kontraktów.
   - Nie modyfikujemy ręcznie wygenerowanego kodu. Kontrolery (`Adapter-in-REST`) implementują wygenerowane delegaty (`*ApiDelegate`).

4. **Wysoka Jakość Kodu**:
   - Kod musi przechodzić testy architektury (`auctions-architecture-tests` używające ArchUnit).
   - Bądź bardzo ostrożny i zwięzły w implementacji. Nie duplikuj kodu. Zawsze szukaj sposobów na wykorzystanie istniejących mechanizmów abstrakcji.

Zawsze sprawdzaj istniejącą strukturę katalogu i kod przed dodaniem nowego katalogu w przestrzeni `core`, aby utrzymać standardy. Jeśli tworzysz nową funkcjonalność, upewnij się, że nie naruszasz granic modułów!

5. **Rejestracja Decyzji (ADR) i Dokumentacja**:
   - Po podjęciu i wprowadzeniu w życie każdej istotnej decyzji architektonicznej bądź biznesowej (np. wybór nowego frameworka lub zmiana zasady dostępu), fakt ten uargumentowany **MUSI zostać udokumentowany**.
   - Odnotuj decyzję w dwóch miejscach: w dokumentacji dla człowieka (w katalogu `/doc`, np. wprowadzając nowy numerowany ADR w oparciu o znane struktury Architecture Decision Records) oraz jeśli stanowi to o tym "jak kod ma powstać", na stałe odzwierciedlenie tej decyzji wyedytuj ten sam nadrzędny plik (`AGENTS.md`) by wiedzę zyskał także przyszły Agent.

6. **Zasady Testowania i Weryfikacji**:
   - Agent zobligowany jest weryfikować poprawność podstawowej logiki poprzez front-end i wywołania End-to-End. Dokładne polecenia jak to zrobić znajdziesz w workflow `/.agents/workflows/agent-testing.md`. Zawsze uruchamiaj lokalnie backend i frontend (dwa osobne i niezależne procesy w tle) na czas trwania iteracji zmian.
   - **Bardzo ważna zasada:** Po każdej wprowadzonej zmianie w oprogramowaniu upewnij się, że nie został uszkodzony proces budowania, logując i śledząc kompilację. Jeżeli zmiana cokolwiek popsuła (jakikolwiek build / lint feedback na czerwono), to ZAWSZE zatrzymaj swoje prace i zajmij się naprawą tego błędu (np. błędy kompilacji klas wygenerowanych przez nowe kontrakty)!
