# Hexabid SPA — Redesign Proposal

## 1. Analiza obecnego stanu

### Zrzuty ekranu (`.local/mockups/`)
- `screenshot-current-home.png` — strona główna (rynek)
- `screenshot-current-sell.png` — formularz tworzenia aukcji
- `screenshot-current-dashboard.png` — dashboard użytkownika

### Identyfikowane problemy

| Obszar | Problem | Priorytet |
|---|---|---|
| **Topbar** | Zaokrąglone nav-pill (border-radius: 999px) wygląda jak dekoracja, nie nawigacja | Wysoki |
| **Hero** | Gradient z promienistymi kolorami (brand-strong + brand-accent) jest zbyt agresywny | Wysoki |
| **Karty** | `card__glow` — absolutnie pozycjonowany gradient wewnątrz karty to dekoracyjny "smak" | Średni |
| **Tipografia** | `Space Grotesk` + `Manrope` — 2 egzotyczne fonty, ładowane z Google Fonts; spowalnia ładownie | Średni |
| **Przyciski** | Gradient na przycisku głównym (`linear-gradient(135deg, ...)`) — nieprzewidywalny, nie-spójny | Wysoki |
| **Border-radius** | `1.75rem` (28px) na kartach i formularzach — zbyt obły jak na aplikację funkcyjną | Średni |
| **Brak design tokens** | Wartości powtarzają się manualnie w wielu plikach SCSS | Wysoki |
| **Mieszanie warstw** | `.surface` klasa z `backdrop-filter: blur(16px)` w globalnym `styles.scss` | Średni |
| **Brak CSS variables** | Kolory hardkodowane np. `#991b1b`, `#166534` w komponentach | Wysoki |
| **Brak dark mode** | Brak zmiennych semantycznych ułatwiających przejście | Niski |

---

## 2. Trzy koncepcje wizualne

### CONCEPT A: Clinical Precision

| Aspekt | Wartość |
|---|---|
| **Font** | Inter (system-ui fallback) |
| **Palette** | Cool grey + blue-500 accent |
| **Radius** | 6–12px (dyskretny) |
| **Topbar** | Flat, pill-segmented nav |
| **Cards** | Clean 1px border, hover shadow |
| **Buttons** | Flat solid, no gradient |
| **Mood** | "Stripe dashboard" — techniczna, racjonalna |

**Zalety:** Najbardziej przewidywalny, GUI elementy nie zaskakują. Idealny jako aplikacja funkcyjna / edukacyjna.
**Wady:** Może wydawać się "zimny" dla aukcji.

---

### CONCEPT B: Warm Professional

| Aspekt | Wartość |
|---|---|
| **Font** | DM Sans + Source Serif 4 (display) |
| **Palette** | Warm stone (#2c2418 ink) + amber accent (#b47a3e) |
| **Radius** | 6–14px (umiarkowany) |
| **Topbar** | Flat z subtelnym accent-light tłem na active |
| **Cards** | Hover: translateY(-2px) + shadow |
| **Buttons** | Flat solid, no gradient |
| **Mood** | "Etsy meets banking" — ciepły, profesjonalny |

**Zalety:** Naturalny dla e-commerce/aukcji. Palette "warm" intuicyjnie pasuje do transakcji.
**Wady:** Serif font może zaskakiwać w edukacyjnym kontekście Angulara.

---

### CONCEPT C: Nordic Calm

| Aspekt | Wartość |
|---|---|
| **Font** | IBM Plex Sans + IBM Plex Serif (display) |
| **Palette** | Stone grey (#1c1917 ink) + violet accent (#7c3aed) |
| **Radius** | 4–10px (minimal) |
| **Topbar** | Classic underline-based nav (bottom-border active) |
| **Cards** | Minimal, 1px border, hover shadow |
| **Buttons** | Flat, compact |
| **Mood** | "Linear.app" — spokojny, bez dekoracji |

**Zalety:** Najczystsza separacja treści od ozdobników. Nav z underline jest najbardziej przewidywalny.
**Wady:** Może wydawać się "surowy".

---

## 3. Rekomendacja

**Wybór: CONCEPT A (Clinical Precision)** z elementami C (underline-nav, compact radius).

**Uzasadnienie:**
1. **Przewidywalność GUI** — Inter + flat buttons + discrete radius = elementy Angulara zachowują się zgodnie z oczekiwaniami
2. **Edukacyjny cel** — czystość ułatwia zrozumienie kodu (bez "magicznych" efektów CSS)
3. **Funkcjonalność** — każda dekoracja (gradient glow, backdrop-filter) to overhead w zrozumieniu
4. **Znajomość** — Inter + blue-500 = wzorzec znany z najbardziej popularnych SaaS dashboardów

---

## 4. Proponowana struktura SPA

```
hexabid-spa/src/
├── app/
│   ├── app.ts                           # Root component (tylko <router-outlet/>)
│   ├── app.html
│   ├── app.scss                          # Pusty (lub :host { display: block })
│   ├── app.config.ts
│   ├── app.routes.ts
│   │
│   ├── core/                             # Infrastruktura aplikacji (nie domenowa)
│   │   ├── layout/
│   │   │   ├── app-shell.component.ts
│   │   │   ├── app-shell.component.html
│   │   │   └── app-shell.component.scss
│   │   ├── session/
│   │   │   └── session.facade.ts
│   │   └── config/
│   │       └── app-endpoints.ts
│   │
│   ├── data-access/                      # Warstwa dostępu do danych (outbound)
│   │   ├── generated/                    # Kod wygenerowany (NIE EDYTOWAĆ)
│   │   │   ├── auction-contract/
│   │   │   ├── auth-contract/
│   │   │   └── payment-contract/
│   │   ├── contracts/                    # View models (VM) — własne typy UI
│   │   │   ├── auction-api.models.ts
│   │   │   └── pricing-api.models.ts
│   │   ├── mappers/                     # Generated → VM mappery
│   │   │   ├── auction-view.mapper.ts
│   │   │   └── pricing-view.mapper.ts
│   │   ├── http/                         # HTTP service layer
│   │   │   ├── auctions-api.service.ts
│   │   │   ├── products-api.service.ts
│   │   │   ├── inventory-api.service.ts
│   │   │   ├── pricing-api.service.ts
│   │   │   └── session-api.service.ts
│   │   └── realtime/
│   │       └── auction-realtime.gateway.ts
│   │
│   ├── features/                         # Feature modules (lazy-loaded)
│   │   ├── home/
│   │   │   ├── home-page.component.ts
│   │   │   ├── home-page.component.html
│   │   │   ├── home-page.component.scss
│   │   │   └── auction-search.facade.ts
│   │   ├── details/
│   │   │   ├── auction-details-page.component.ts
│   │   │   ├── auction-details-page.component.html
│   │   │   ├── auction-details-page.component.scss
│   │   │   └── auction-details.facade.ts
│   │   ├── create/
│   │   │   ├── auction-create-page.component.ts
│   │   │   ├── auction-create-page.component.html
│   │   │   └── auction-create-page.component.scss
│   │   ├── pricing/
│   │   │   ├── pricing-page.component.ts
│   │   │   ├── pricing-page.component.html
│   │   │   ├── pricing-page.component.scss
│   │   │   └── pricing.facade.ts
│   │   ├── dashboard/
│   │   │   ├── user-dashboard-page.component.ts
│   │   │   ├── user-dashboard-page.component.html
│   │   │   └── user-dashboard-page.component.scss
│   │   ├── products/
│   │   │   ├── product-catalog-page.component.ts
│   │   │   ├── product-catalog-page.component.html
│   │   │   ├── product-catalog-page.component.scss
│   │   │   ├── product-details-page.component.ts
│   │   │   ├── product-details-page.component.html
│   │   │   └── product-details-page.component.scss
│   │   └── inventory/
│   │       ├── batch-create-page.component.ts
│   │       ├── batch-create-page.component.html
│   │       ├── batch-create-page.component.scss
│   │       ├── instance-manager-page.component.ts
│   │       ├── instance-manager-page.component.html
│   │       └── instance-manager-page.component.scss
│   │
│   └── shared/                           # Shared UI primitives
│       └── ui/
│           ├── auction-card.component.ts
│           ├── auction-card.component.html
│           └── auction-card.component.scss
│           ├── empty-state.component.ts
│           ├── empty-state.component.html
│           └── empty-state.component.scss
│
├── styles/                               # ← NOWE: Global style architecture
│   ├── _tokens.scss                      # Design tokens (CSS custom properties)
│   ├── _reset.scss                       # Minimal CSS reset
│   ├── _typography.scss                  # Font system, heading scale, body text
│   ├── _base.scss                        # body, a, hr, etc.
│   ├── _components.scss                  # Global component primitives (btn, input, surface)
│   └── _utilities.scss                   # Utility classes (if any)
│
├── index.html
├── main.ts
└── styles.scss                           # ← Główny entry: @use all partials
```

### Zasady separacji

| Warstwa | Odpowiada za | Zależności |
|---|---|---|
| `styles/` | Tokeny, reset, typografia, prymitywy | Nic (0 deps) |
| `core/` | Shell, session, config | `data-access/` |
| `data-access/` | HTTP, mappery, VM | `generated/` (1-way) |
| `features/` | Strony i ich facady | `data-access/`, `shared/ui/` |
| `shared/ui/` | Reużywalne komponenty UI | Nic (pure presentational) |

---

## 5. CSS Architecture — Design Tokens

```scss
// _tokens.scss
:root {
  // ── Color Palette ──
  --color-blue-50:  #eff6ff;
  --color-blue-100: #dbeafe;
  --color-blue-500: #3b82f6;
  --color-blue-600: #2563eb;
  --color-blue-700: #1d4ed8;

  --color-green-50:  #ecfdf5;
  --color-green-600: #059669;
  --color-green-700: #047857;

  --color-red-50:  #fef2f2;
  --color-red-600: #dc2626;
  --color-red-700: #b91c1c;

  --color-gray-50:  #f9fafb;
  --color-gray-100: #f3f4f6;
  --color-gray-200: #e5e7eb;
  --color-gray-300: #d1d5db;
  --color-gray-400: #9ca3af;
  --color-gray-500: #6b7280;
  --color-gray-600: #4b5563;
  --color-gray-700: #374151;
  --color-gray-800: #1f2937;
  --color-gray-900: #111827;

  // ── Semantic Colors ──
  --bg-primary:     var(--color-gray-50);
  --bg-surface:     #ffffff;
  --bg-elevated:    #ffffff;

  --border-default:  var(--color-gray-200);
  --border-subtle:   var(--color-gray-100);
  --border-focus:    var(--color-blue-500);

  --text-primary:    var(--color-gray-900);
  --text-secondary:  var(--color-gray-500);
  --text-muted:      var(--color-gray-400);

  --accent:          var(--color-blue-500);
  --accent-hover:    var(--color-blue-600);
  --accent-light:    var(--color-blue-50);

  --success:         var(--color-green-600);
  --success-bg:      var(--color-green-50);
  --danger:          var(--color-red-600);
  --danger-bg:       var(--color-red-50);

  // ── Typography ──
  --font-body:    'Inter', system-ui, -apple-system, sans-serif;
  --font-mono:    'JetBrains Mono', ui-monospace, monospace;

  --text-xs:   0.75rem;    // 12px
  --text-sm:   0.8125rem;  // 13px
  --text-base: 0.875rem;   // 14px
  --text-md:   1rem;       // 16px
  --text-lg:   1.125rem;   // 18px
  --text-xl:   1.25rem;    // 20px
  --text-2xl:  1.5rem;     // 24px
  --text-3xl:  1.875rem;   // 30px

  --leading-tight:  1.25;
  --leading-normal: 1.5;
  --leading-relaxed: 1.625;

  // ── Spacing (4px grid) ──
  --space-1:  0.25rem;  // 4px
  --space-2:  0.5rem;   // 8px
  --space-3:  0.75rem;  // 12px
  --space-4:  1rem;     // 16px
  --space-5:  1.25rem;  // 20px
  --space-6:  1.5rem;   // 24px
  --space-8:  2rem;     // 32px
  --space-10: 2.5rem;   // 40px
  --space-12: 3rem;     // 48px

  // ── Radius ──
  --radius-sm: 4px;
  --radius-md: 6px;
  --radius-lg: 8px;
  --radius-xl: 12px;
  --radius-full: 9999px;

  // ── Shadows ──
  --shadow-xs:  0 1px 2px rgba(0,0,0,0.05);
  --shadow-sm:  0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  --shadow-md:  0 4px 6px -1px rgba(0,0,0,0.07), 0 2px 4px -2px rgba(0,0,0,0.05);
  --shadow-lg:  0 10px 15px -3px rgba(0,0,0,0.08), 0 4px 6px -4px rgba(0,0,0,0.04);

  // ── Transitions ──
  --transition-fast:   120ms ease;
  --transition-normal: 180ms ease;
  --transition-slow:   280ms ease;
}
```

---

## 6. Elementy GUI — zasady przewidywalności

| Element | Zasada | Przykład |
|---|---|---|
| **Przycisk** | Flat solid color, no gradient | `background: var(--accent)` |
| **Input** | 1px border, focus ring | `border: 1px solid var(--border-default)` + `box-shadow: 0 0 0 3px var(--accent-light)` |
| **Karta** | 1px border, hover = shadow elevation | `border: 1px solid var(--border-default)` → `box-shadow: var(--shadow-md)` |
| **Nawigacja** | Underline lub discrete pill (nie: pill-999px) | Bottom border lub bg-highlight |
| **Badge/Tag** | Subtle bg + matching text | `bg: var(--success-bg)`, `color: var(--success)` |
| **Surface** | Biały prostokąt z border | Nigdy: gradient, backdrop-filter, glow |

---

## 7. Następne kroki

1. Wybór koncepcji (A / B / C / hybryda)
2. Implementacja `styles/_tokens.scss`
3. Migracja `styles.scss` → `styles/` partials
4. Refaktor komponentów SCSS (usunięcie gradientów, glow, hardkodowanych kolorów)
5. Ujednolicenie topbar i nawigacji
6. Utworzenie Style Guide (`.local/style-guide/`)
7. Aktualizacja wiki
