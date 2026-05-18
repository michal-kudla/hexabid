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

**Domena**: `hexabid-quantity`, `hexabid-product`, `hexabid-inventory`, `hexabid-core`, `hexabid-auth-core`, `hexabid-payment-core`, `hexabid-pricing`, `hexabid-rules`.

**Adaptery wejściowe**: `hexabid-adapter-in-rest`, `hexabid-adapter-in-ws`, `hexabid-adapter-in-job`, `hexabid-adapter-in-auth-oauth`, `hexabid-adapter-in-auth-local`.

**Adaptery wyjściowe**: `hexabid-adapter-out-db`, `hexabid-adapter-out-kafka`, `hexabid-adapter-out-kyc`, `hexabid-adapter-out-kyc-local`, `hexabid-payment-adapter-{payu|p24|crypto|local}`.

**Warstwy techniczne**: `hexabid-api-contract`, `hexabid-payment-api-contract`, `hexabid-bootstrap`, `hexabid-architecture-tests`, `hexabid-integration-tests`.

**Frontend**: `hexabid-spa` - Angular 20 SPA z wygenerowanym klientem TypeScript z OpenAPI.

### Reguły architektury

Egzekwowane przez `hexabid-architecture-tests`:

- **Moduły domenowe nie mogą zależeć od Springa ani JPA**: `@NullMarked` (jspecify) zamiast null-safety frameworku
- **Separacja kodu wygenerowanego i ręcznego**: Wygenerowany kod dostaje swoje package'i (`com.github.hexabid.contract.*`), ręczny kod to `com.github.hexabid.adapter.*`
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
- Java: `hexabid-api-contract/target/generated-sources/openapi/`
- TypeScript: `hexabid-spa/src/app/data-access/generated/`
- Regeneruj: `mvn -pl hexabid-api-contract generate-sources` (z repo root)
- Frontend sync: `npm run contract:sync` (z `hexabid-spa/`)

**Ręczny kod** (ownerszy):
- Java REST delegates: `hexabid-adapter-in-rest/src/main/java/com/github/hexabid/adapter/in/rest/`
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
mvn -f hexabid-bootstrap/pom.xml spring-boot:run -Dspring-boot.run.profiles=local

# Integration tests (wymagają uruchomionego backendu na :18080)
mvn -f hexabid-integration-tests/pom.xml verify

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

Czytaj najpierw `ai/wiki/index.md`, a dla lokalnych profili `ai/wiki/PROFIL_LOCAL_GUIDE.md`. Aktualizuj `ai/wiki/log.md` i decyzje w `ai/wiki/decisions/` po istotnych zmianach.

---

## **NIE PSUJ UWIERZYTELNIANIA**

**Ta sekcja jest krytyczna. Uwierzytelnianie było psute wielokrotnie przez nieświadome zmiany. Przeczytaj i stosuj.**

**ZABRONIONE zmiany w `LocalSecurityConfiguration`:**

- **NIE UŻYWAJ `SessionCreationPolicy.STATELESS`** — formLogin i `/login/dev` wymagają sesji HTTP. JWT jest dodatkowym mechanizmem, NIE zastępuje sesji.
- **NIE UŻYWAJ `httpBasic` W OGÓLE** — Spring Security 7 ignoruje `HttpStatusEntryPoint` w `httpBasic()` i wysyła `WWW-Authenticate: Basic`, co: (1) wywołuje natywny dialog logowania w przeglądarce, (2) blokuje OAuth2 redirecty (Chrome: `ERR_INVALID_AUTH_CREDENTIALS`). Profil local używa formLogin + oauth2Login — httpBasic nie jest potrzebny.
- **NIE USUWAJ `exceptionHandling` z `HttpStatusEntryPoint(UNAUTHORIZED)`** — bez tego Spring Security przekierowuje na `/login` zamiast zwrócić 401 dla API, co psuje SPA.
- **NIE USUWAJ `formLogin`** — formLogin jest wymagane dla działania `/login/dev`.
- **NIE USUWAJ `oauth2Login`** — oauth2Login jest wymagane dla `/oauth2/authorization/dev`.
- **NIE USUWAJ `permitAll()` dla `/login/**`, `/logout`, `/dev-auth/**`** — te endpointy muszą być publicznie dostępne.
- **NIE USUWAJ `PasswordEncoder` bean** — `User.withDefaultPasswordEncoder()` używa `DelegatingPasswordEncoder` (`{bcrypt}` prefix). Bez tego bean'a Basic auth nie działa.

**Testy zabezpieczające** (`AuthSecurityIT`): SEC11 weryfikuje brak `WWW-Authenticate` na API 401; SEC12 weryfikuje że OAuth2 redirect nie jest blokowany. Jeśli te testy nie przechodzą — uwierzytelnianie jest psute.

**Szczegóły**: `ai/wiki/decisions/2026-05-05-dev-auth-e2e.md`
