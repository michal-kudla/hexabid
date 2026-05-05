# Tytuł
Ujednolicenie lokalnych adresów backendu i SPA.

## Podsumowanie (TLDR)
- Lokalny backend Hexabid działa pod `http://localhost:18080/hexabid`.
- Lokalna SPA działa pod `http://localhost:14200`.
- Konfiguracje, kontrakty i dokumentacja nie powinny wskazywać na stare porty sprzed profilu `local`.

## Tagi
#configuration #local-profile #ports #systemd #openapi #spa

## Treść
Decyzja porządkuje lokalne adresy uruchomieniowe po migracji na profil `local` i systemd user services.

Zasady:
1. Backend:
   - URL bazowy: `http://localhost:18080/hexabid`.
   - WebSocket STOMP: `ws://localhost:18080/hexabid/ws-auctions`.
   - Systemd service: `hexabid-backend`, uruchamiany z `--spring.profiles.active=local`.
2. Frontend:
   - Angular dev server: `http://localhost:14200`.
   - Proxy SPA kieruje `/api`, `/oauth2`, `/login`, `/logout` i `/ws-auctions` do backendu na `18080/hexabid`.
3. Kontrakt:
   - OpenAPI `servers.url` dla auction API wskazuje `http://localhost:18080/hexabid`.
   - Utrzymywane w repo runtime'y klienta TypeScript nie mogą mieć fallbacku na stary port backendu.
4. Integracje zewnętrzne:
   - KYC nie ma lokalnego hardcoded portu; domyślnie używa `KYC_BASE_URL` albo neutralnego `https://kyc.example.test`.
   - Dev OIDC używa zmiennych `DEV_OIDC_*`; domyślne wartości nie wskazują już starego portu aplikacji.

Walidacja 2026-05-05:
- `rg` nie znajduje tekstowych odniesień do starych portów ani starych modułów kontraktu/bootstrapu.
- `mvn -Plocal -DskipTests install` przechodzi.
- `npm run build` przechodzi.
- `systemctl --user restart hexabid-backend` uruchamia backend na profilu `local`.
- `npm run e2e:business` przechodzi 4/4.

Powiązania:
- [[decisions/2026-04-16-profiles-local]]
- [[decisions/2026-05-04-spa-e2e-business-flow]]
- [[PROFIL_LOCAL_GUIDE]]
