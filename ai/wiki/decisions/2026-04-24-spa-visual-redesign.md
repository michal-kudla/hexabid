# ADR: SPA Visual Redesign — Concept B (Warm Professional) + Accessibility

**Date**: 2026-04-24
**Status**: Accepted
**Tags**: #spa #design #accessibility #css

## TLDR

Przejście z "glossy" look-and-feel (Space Grotesk + Manrope, aggressive gradients, 1px borders) na **Warm Professional** (DM Sans + Source Serif 4, flat buttons, 2px borders, WCAG AA compliance). Wszystkie kolory hard-coded zastąpione design tokens w `src/styles/_tokens.scss`.

## Kontekst

Obecny SPA miał:
- Gradient na przyciskach i hero — nieprzewidywalny, rozprasza
- 1px border — niewidoczny dla niedowidzących
- Hard-coded kolory (#991b1b, #166534) w komponentach
- Backdrop-filter: blur — dekoracyjny overhead
- card__glow — absolutnie pozycjonowany gradient dekoracyjny
- Pill-nav (border-radius: 999px) — nie jest standardowym wzorcem nawigacji

## Decyzja

### Paleta: Stone + Amber
- **Ink**: `#1f1a12` (stone-900) zamiast `#0f172a`
- **Accent**: `#a86514` (amber-600) zamiast `#0b5ed7`
- **Surface**: biały z 2px border zamiast blur-glass

### Fonty
- **Body**: DM Sans (zamiast Manrope)
- **Display**: Source Serif 4 (zamiast Space Grotesk)
- **Mono**: system monospace (bez Google Fonts ładowania)

### Dostępność (WCAG AA)
- **2px border** na inputs, cards, badges (zamiast 1px)
- **min-height: 44px** na touch targets (zamiast braku)
- **Badge z border** (widoczny na każdym tle)
- **Alert z border-left: 4px solid** (clear indicator)
- **Focus ring: 0 0 0 4px** (visible focus)
- **--ink-secondary: stone-600 (~6.5:1)** zamiast stone-400 (~3.2:1)

### CSS Architecture
```
src/styles/
  _tokens.scss     # CSS custom properties (jedno źródło prawdy)
  _reset.scss      # Minimal CSS reset
  _components.scss # Button, input, select, label
  _utilities.scss  # .surface, .kicker, .section-heading, .alert-inline
```

### Nawigacja
- **Underline-based** (bottom-border na active) zamiast pill-segmented

## Konsekwencje

(+) Zmiana paletry wymaga edycji 1 pliku (_tokens.scss)
(+) Wszystkie kontrasty spełniają WCAG AA
(+) Elementy GUI są przewidywalne (flat buttons, clear borders)
(-) Brak ciemnego motywu (przygotowany przez tokeny, ale nie zaimplementowany)
(-) Source Serif 4 wymaga Google Fonts ładowania

## Pliki zmienione

- `src/styles/` — nowa architektura CSS
- `src/styles.scss` — entry point
- `src/index.html` — nowe fonty
- `src/app/core/layout/app-shell.component.*` — topbar + nav
- `src/app/shared/ui/auction-card.component.*` — karty
- `src/app/shared/ui/empty-state.component.*` — empty state
- `src/app/features/home/home-page.component.*` — rynek
- `src/app/features/details/auction-details-page.component.*` — szczegóły
- `src/app/features/create/auction-create-page.component.*` — formularz
- `src/app/features/pricing/pricing-page.component.*` — kalkulacja
- `src/app/features/dashboard/user-dashboard-page.component.*` — dashboard
- `src/app/features/products/*.ts` — inline styles tokens update
- `src/app/features/inventory/*.ts` — inline styles tokens update
