# Hexabid

Wzorcowa aplikacja aukcyjna z architekturą heksagonalną, podejściem contract-first i "krzyczącą" domeną.

## Moduły

- `hexabid-core` - czysta domena i use case'y bez Springa/JPA, wraz ze wspólnymi archetypami biznesowymi jak `Party`, `Product` i `Lot`.
- `hexabid-auth-core` - model uwierzytelnionego użytkownika i port dostępu do aktualnej tożsamości, mapowanej na domenowe `PartyId`.
- `hexabid-api-contract` - kontrakt OpenAPI dla wejścia REST i generowane DTO/interfejsy.
- `hexabid-adapter-in-auth-oauth` - adapter Spring Security OAuth2/OpenID Connect z dostawcami Google i GitHub.
- `hexabid-adapter-in-rest` - implementacja wygenerowanego delegate REST.
- `hexabid-adapter-in-ws` - inbound WebSocket dla licytacji real-time.
- `hexabid-adapter-in-job` - scheduler zamykający przeterminowane aukcje.
- `hexabid-adapter-out-db` - adapter JPA implementujący port repozytorium.
- `hexabid-adapter-out-kafka` - publikacja zdarzeń domenowych do Kafki.
- `hexabid-adapter-out-kyc` - klient KYC wygenerowany z kontraktu OpenAPI.
- `hexabid-bootstrap` - composition root i uruchamialna aplikacja Spring Boot.
- `hexabid-architecture-tests` - reguły ArchUnit pilnujące granic architektury.

## Uruchomienie

```bash
mvn clean verify
mvn -f hexabid-bootstrap/pom.xml spring-boot:run -Dspring-boot.run.profiles=local
```

REST startuje pod `http://localhost:18080/hexabid`, WebSocket STOMP pod `ws://localhost:18080/hexabid/ws-auctions`.

### Profil lokalny z danymi demo

Profil `local` zasila pustą bazę H2 przykładowymi aukcjami gotowymi do przeglądania w SPA.

Uruchomienie:

```bash
mvn -Plocal -DskipTests install
systemctl --user restart hexabid-backend
```

W tym profilu:

- seed danych jest włączony
- publiczne `GET /api/auctions` i `GET /api/auctions/{id}` są dostępne do przeglądania rynku
- frontend SPA działa pod `http://localhost:14200`

Developer login:

- otwórz `http://localhost:18080/hexabid/login/dev`
- wybierz jednego z użytkowników demo
- po zalogowaniu możesz zmieniać użytkownika z poziomu tego samego ekranu lub z linku `Zmien usera` w SPA
- użytkownicy oznaczeni jako `KYC blocked` pozwalają testować scenariusze negatywne

## Uwierzytelnianie

Aplikacja używa OAuth2 / OpenID Connect:

- Google
- GitHub

Konfiguracja klientów jest w `hexabid-bootstrap/src/main/resources/application.yaml` i korzysta ze zmiennych:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GITHUB_CLIENT_ID`
- `GITHUB_CLIENT_SECRET`

Tożsamość użytkownika nie jest już przekazywana w payloadach REST/WS. Adaptery wejściowe pobierają ją z kontekstu uwierzytelnienia.

## Dokumentacja

- architektura C4: `doc/architecture-c4.adoc`
- instrukcja użytkownika: `doc/user-guide.adoc`

Renderowanie:

```bash
./doc/render.sh
```
