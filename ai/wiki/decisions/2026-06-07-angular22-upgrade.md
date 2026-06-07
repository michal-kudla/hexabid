# ADR: Angular 22 Upgrade for hexabid-spa

## Data
2026-06-07

## Status
Zatwierdzone

## Kontekst
Projekt hexabid-spa używał Angular 20.3.0. Angular 22 wprowadza nowe funkcje i ulepszenia wydajności, w tym:
- Standalone components jako domyślne
- Ulepszone signals i reactivity
- Lepsze SSR/hydration
- Nowsze TypeScript 6.x support
- Ulepszone Angular CLI z Vite/esbuild

## Decyzja
Zaktualizować hexabid-spa do Angular 22.0.0 z TypeScript 6.0.3.

## Szczegóły implementacji
1. Zaktualizowano wszystkie `@angular/*` dependencies do `22.0.0` w `hexabid-spa/package.json`
2. Zaktualizowano devDependencies: `@angular/build`, `@angular/cli`, `@angular/compiler-cli` do `22.0.0`
3. Zaktualizowano TypeScript do `6.0.3` (wymagany przez Angular 22)
4. Wymagano Node.js >=24.15.0 (zainstalowano przez nvm v24.15.0)
5. Uruchomiono `npm install` i zweryfikowano build
6. Uruchomiono wszystkie 53 Playwright e2e testy - wszystkie przeszły
7. Angular dev server działa na porcie 14200

## Ostrzeżenia kompilatora (niekrytyczne)
- NG8112: nieużywana zmienna `@let a` w `auction-bid-panel.component.html`
- NG8113: nieużywany `RouterLink` w `InstanceManagerPageComponent`
- NG8107: opcjonalne łańcuchy (`?.`) które mogą być zwykłymi kropkami w `product-details-page.component.ts`

## MCP Capability
Badano możliwość `@angular/mcp` - nie istnieje taki pakiet w npm. Angular 22 nie wprowadza osobnego "Module Communication Protocol" jako pakietu. Komunikacja modułów w Angular 22 nadal opiera się na standardowych wzorcach: services, signals, RxJS, lazy loading.

## Wpływ na inne części
- Backend (Java/Spring) niezmieniony
- OpenAPI contract generation niezmieniony (`npm run contract:sync` działa)
- Systemd user services niezmienione
- Architecture tests (ArchUnit) niezmienione

## Tagi
#angular22 #upgrade #frontend #typescript6 #migration