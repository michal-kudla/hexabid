# AGENTS.md - LLM WIKI OPERATIONAL GUIDE

Jesteś administratorem i twórcą LLM WIKI dla tego projektu. Twoim zadaniem jest utrzymywanie, rozwijanie i pielęgnowanie wiedzy projektowej w folderze `ai/wiki/`.

## 1. ZASADY ZARZĄDZANIA WIEDZĄ
* **Source of Truth**: Każda nowa decyzja architektoniczna, naprawiony błąd czy odkryty wzorzec musi trafić do wiki.
* **Struktura plików**: Używaj formatu: `Tytuł`, `Podsumowanie (TLDR)`, `Tagi`, `Treść`.
* **Cross-referencing**: Linkuj dokumenty między sobą za pomocą formatu `[[nazwa-pliku.md]]`.

## 2. PROCEDURA PIELĘGNACJI (INGESTION FLOW)
Po zakończeniu każdego istotnego zadania lub sesji wykonaj:

1. **Analiza sesji**: Wyodrębnij kluczowe fakty (decyzje, konfiguracje, unikalne rozwiązania).
2. **Aktualizacja index.md**: Zaktualizuj `ai/wiki/index.md` o nowe wpisy z krótkim opisem.
3. **Logowanie**: Dopisz wpis do `ai/wiki/log.md` w formacie `## [DATA] [TYP] Opis zmiany`.
4. **Flagi sprzeczności**: Jeśli nowa wiedza zaprzecza starej, oznacz to w starym dokumencie i poproś użytkownika o rozstrzygnięcie.

## 3. PROGRESYWNE UJAWNIANIE (TOKEN ECONOMY)
* Utrzymuj AGENTS.md poniżej 150 linii.
* Zamiast kopiować całą dokumentację do tego pliku, umieszczaj tu tylko mapę drogową i linki do specyficznych plików w `ai/wiki/`, które przeczytasz w razie potrzeby.

## 4. ROLE I KONWENCJE
* **Role**: Zachowuj się jak starszy programista dbający o dokumentację.
* **Styl**: Pisz instrukcje operacyjne (polecenia), a nie opisy literackie.

## Kluczowe komponenty LLM WIKI
* `ai/wiki/index.md`: Katalog wszystkich stron, który agent skanuje jako pierwszy.
* `ai/wiki/log.md`: Chronologiczny zapis zmian i postępów w projekcie.
* `ai/wiki/concepts/`: Definicje trudnych pojęć specyficznych dla domeny projektu.
* `ai/wiki/decisions/`: Rejestr decyzji architektonicznych (tzw. ADR – Architecture Decision Records).

---

# Architektura

Hexabid to aplikacja aukcyjna wzorcowa z **architekturą heksagonalną** (hexagonal/ports-adapters). 

### Struktura modułów

**Moduły domenowe** (czyste Java, bez framework'u):
- `quantity` - jednostki i wartości ilościowe
- `product` - katalog produktów (ProductType, PackageType)
- `inventory` - zarządzanie inwentarzem
- `auctions-core` - domena aukcji (Auction, Bid, Lot)
- `auctions-auth-core` - model Party i uwierzytelniania
- `auctions-payment-core` - przetwarzanie płatności

**Porty i adaptery wejściowe** (inbound):
- `auctions-adapter-in-rest` - REST API (Spring MVC + OpenAPI generowane)
- `auctions-adapter-in-ws` - WebSocket STOMP dla licytacji real-time
- `auctions-adapter-in-job` - scheduler dla zamykania przeterminowanych aukcji
- `auctions-adapter-in-auth-oauth` - OAuth2/OpenID Connect (Google, GitHub)
- `auctions-adapter-in-auth-local` - local form login (dev)

**Porty i adaptery wyjściowe** (outbound):
- `auctions-adapter-out-db` - JPA/Hibernate persistence
- `auctions-adapter-out-kafka` - publikacja domenowych zdarzeń
- `auctions-adapter-out-kyc` - external KYC verification (klient OpenAPI)
- `auctions-adapter-out-kyc-local` - local KYC mock (dev)
- `auctions-payment-adapter-{payu|p24|crypto|local}` - payment gateways

**Warstwy techniczne**:
- `auctions-api-contract` - OpenAPI (auction, auth, payment) + kod generowany
- `auctions-bootstrap` - composition root i Spring Boot entry point
- `auctions-architecture-tests` - reguły ArchUnit pilnujące granic
- `auctions-integration-tests` - end-to-end tests na HTTP + WebSocket

**Frontend**:
- `hexabid-spa` - Angular 20 SPA z wygenerowanym klientem TypeScript z OpenAPI

### Reguły architektury

Egzekwowane przez `auctions-architecture-tests`:

- **Moduły domenowe nie mogą zależeć od Springa ani JPA**: `@NullMarked` (jspecify) zamiast null-safety frameworku
- **Separacja kodu wygenerowanego i ręcznego**: Wygenerowany kod dostaje swoje package'i (`com.acme.auctions.contract.*`), ręczny kod to `com.acme.auctions.adapter.*`
- **Dependency injection tylko w adapterach**: Domeny są prostymi klasami bez Spring annotacji

## Reguły nazewnictwa

### HTTP Headers - `X-API-Version`

Dla zmiennych/parametrów reprezentujących nagłówek `X-API-Version` używaj zapisu **`xApiVersion`**.
- ✅ Prawidłowo: `xApiVersion` (camelCase, mała litera 'A')
- ❌ Nieprawidłowo: `xAPIVersion` (camelCase ale wielka litera 'A')

**Zasada dotyczy**:
- Kodu pisanego ręcznie
- Plików wygenerowanych utrzymywanych w repo (OpenAPI YAML definitions)
- Wygenerowanego kodu **TYLKO jeśli jest utrzymywany w repo** — domyślnie wygenerowany kod jest ignorowany w .gitignore

**Uwaga**: Aktualnie OpenAPI generator produkuje `xAPIVersion` zarówno dla Java jak TypeScript. To jest bug generatora — gdy będzie można wpłynąć na konfigurację (np. custom templates), należy to naprawić, aby wszystkie nowoetworzone pliki generowane dla tego nagłówka używały `xApiVersion`.

### Kod wygenerowany vs ręczny

**Wygenerowany kod** (nigdy nie edituj bezpośrednio):
- Java: `auctions-api-contract/target/generated-sources/openapi/`
- TypeScript: `hexabid-spa/src/app/data-access/generated/`
- Regeneruj: `mvn -pl auctions-api-contract generate-sources` (z repo root)
- Frontend sync: `npm run contract:sync` (z `hexabid-spa/`)

**Ręczny kod** (ownerszy):
- Java REST delegates: `auctions-adapter-in-rest/src/main/java/com/acme/auctions/adapter/in/rest/`
- TypeScript façades/mappers: `hexabid-spa/src/app/data-access/`

## Konwencje OpenAPI

- Wszystkie headery wymagające wersji API: referencja do `#/components/parameters/ApiVersionHeader`
- Definicja w `components.parameters`:
  ```yaml
  ApiVersionHeader:
    in: header
    name: X-API-Version
    required: false
    schema:
      type: string
      default: "1"
    description: API version negotiated via HTTP header.
  ```
- Generowanie: `mvn clean verify` (regeneruje Java i TypeScript)

## Uruchomienie i Testy

```bash
# Backend build i run
mvn clean verify
mvn -f auctions-bootstrap/pom.xml spring-boot:run

# Backend dev (z demo data)
mvn -f auctions-bootstrap/pom.xml spring-boot:run -Dspring-boot.run.profiles=dev

# Integration tests (wymagają uruchomionego backendu na :18080)
mvn -f auctions-integration-tests/pom.xml verify

# Frontend
cd hexabid-spa
npm install
npm start

# Sync OpenAPI contract (gdy zmienisz YAML)
npm run contract:sync
```

## Stack Techniczny

- **Java 25** z jspecify dla null-safety (bez Spring)
- **Spring Boot 4.0.3** w adapterach
- **Spring Data JPA** (Hibernate) w outbound adapter
- **Spring WebSocket** + STOMP w inbound adapter
- **OpenAPI Generator 7.14.0** (spring i typescript-fetch)
- **Angular 20** w frontend
- **Maven** multi-module
- **ArchUnit 1.4.1** dla architecture tests

## Dokumentacja dla Agentów AI

**Katalog `ai/`** zawiera dokumentację w formacie Markdown przeznaczoną dla agentów AI/LLM:

- `ai/PROFIL_LOCAL_GUIDE.md` - Szczegółowy przewodnik po profilach Maven i Spring dla lokalnego developmentu
- `ai/DOCUMENTATION_STRUCTURE.md` - Organizacja dokumentacji w projekcie
- Dokumentacja ta zawiera informacje techniczne, przykłady uruchomienia i konfiguracje potrzebne do kontynuowania pracy nad projektem

**Uwagi dla agentów AI:**
- Dokumentacja w `ai/` jest przeznaczona do szybkiego zrozumienia kontekstu projektu
- Zawiera informacje o zmianach, konfiguracjach i procedurach uruchomienia
- Jest aktualizowana przy istotnych zmianach w projekcie
- Format Markdown ułatwia parsowanie przez LLM
