# Archetypy architektoniczne (2024–2026) dla pełnoprawnego systemu aukcyjnego

**Data:** 2026-03-30  
**Kontekst:** odpowiedź na macierz priorytetów funkcjonalnych (notyfikacje, audit, proxy bidding, anulowanie, discovery, reklamacje, rating, payment timeout, geolokacja, reporting).

## 1) Co jest dziś „nowoczesnym” archetypem e-commerce (a nie legacy)

Na bazie aktualnych materiałów vendorów/platform (AWS, Stripe, Google Cloud, eBay, MACH/composable) najbardziej praktyczny zestaw archetypów to:

1. **Composable Commerce / MACH Backbone**
   - system budowany jako zestaw autonomicznych capabilities (pricing, bidding, payments, trust, notifications), spiętych API i eventami,
   - celem jest wymienialność komponentów i szybka ewolucja domeny.

2. **Event-Driven Core + Transactional Outbox**
   - wszystkie kluczowe zmiany domenowe emitują zdarzenia,
   - publikacja zdarzeń jest odporna na awarie (outbox),
   - konsumenci są idempotentni.

3. **Saga / Process Manager dla długich transakcji**
   - tam gdzie proces trwa długo i ma kompensacje (np. wygrana -> płatność -> timeout -> rekompensata),
   - jawnie modelowane kroki sukces/failure.

4. **Payment Intent + Idempotent Webhook Processing**
   - płatność modelowana jako lifecycle (nie pojedyncze wywołanie),
   - callbacki rozliczane idempotentnie, z retry-aware semantyką.

5. **Trust & Safety / Reputation Archetype**
   - osobny bounded context dla score’ów reputacji, feedbacku, sygnałów ryzyka i zasad moderacji,
   - decyzje antyfraudowe i quality gates są mierzalne i audytowalne.

6. **Discovery Intelligence Archetype (Search + Reco + User Events)**
   - wyszukiwanie, faceting i rekomendacje oparte o eventy zachowań użytkownika,
   - ranking i personalizacja jako osobny capability.

7. **Case Management Archetype (Disputes/Claims)**
   - reklamacje i spory jako workflow case’owy (statusy, SLA, eskalacje, dowody),
   - odseparowany od samego „happy path” licytacji.

8. **Analytics/Lakehouse Archetype dla raportowania operacyjnego i BI**
   - zdarzenia transakcyjne + read modele analityczne,
   - eksport i dashboarding bez obciążania ścieżek online.

## 2) Mapowanie Twojej macierzy na archetypy

## CRITICAL

### Notyfikacje
**Archetypy:**
- Event-Driven Core + Outbox,
- Omnichannel Notification Service (template + preference + throttling).

**Minimum implementacyjne:**
- eventy: `BID_OUTBID`, `AUCTION_ENDING_SOON`, `AUCTION_WON`, `PAYMENT_TIMEOUT_WARNING`,
- kanały: email/push/websocket,
- deduplikacja i idempotencja wysyłek,
- user preferences + quiet hours.

## HIGH

### Historia / Audit
**Archetypy:**
- Audit/Event Journal,
- Immutable append-only log + correlation IDs.

**Minimum implementacyjne:**
- pełen łańcuch: komenda -> decyzja -> event -> efekt,
- actor identity + timestamp + reason codes,
- query API pod compliance i anomaly detection.

### Proxy Bidding
**Archetypy:**
- Auction Decision Engine (domain policy engine),
- deterministic rules + optimistic locking.

**Minimum implementacyjne:**
- max-bid per użytkownik,
- auto-raise z krokiem (increment ladder),
- tie-break rule (np. earlier max bid wins),
- odporność na race conditions.

### Anulowanie aukcji
**Archetypy:**
- Saga/Process Manager (kompensacje),
- Policy Engine (kiedy wolno anulować i jakie konsekwencje).

**Minimum implementacyjne:**
- stany: `CANCEL_REQUESTED`, `CANCELLED`, `CANCEL_REJECTED`,
- reguły zależne od czasu i liczby ofert,
- automatyczne notyfikacje i ewentualne opłaty/penalty,
- audyt decyzji anulowania.

## MEDIUM

### Kategorie / Szukanie
**Archetypy:**
- Discovery Intelligence (search index + recommendation service),
- CQRS read models pod listingi.

**Minimum implementacyjne:**
- kategorie, facety, paginacja cursor-based,
- sortowanie i boosting „ending soon / relevance”,
- rekomendacje oparte o user events.

### Reklamacje
**Archetypy:**
- Case Management,
- workflow + SLA + evidences.

**Minimum implementacyjne:**
- otwarcie sprawy, statusy, terminy odpowiedzi,
- załączniki/dowody,
- decyzja: refund/partial refund/reject,
- ścieżka mediacja -> arbitraż.

### Rating
**Archetypy:**
- Reputation Service + Trust Score,
- anti-abuse rules (cooldown, verified transaction gating).

**Minimum implementacyjne:**
- rating tylko po zakończonej transakcji,
- agregaty per seller/buyer,
- wykrywanie anomalii (spam/revenge patterns),
- wpływ ratingu na ranking i limity.

### Payment Timeout
**Archetypy:**
- Saga + deadline timers,
- Payment Intent lifecycle.

**Minimum implementacyjne:**
- deadline płatności na poziomie wygranej aukcji,
- remindery przed timeout,
- po timeout: kompensacja (next bidder / relist),
- pełna idempotencja webhooków i retry-safe processing.

## LOW

### Geolokacja
**Archetypy:**
- Policy-as-code dla geo-rules,
- risk engine (IP/device heuristics).

**Minimum implementacyjne:**
- geo restrictions per kraj/region,
- podstawy tax/regulatory routing,
- flagowanie ryzykownych sesji.

### Reporting
**Archetypy:**
- Analytics pipeline (CDC/event stream -> warehouse),
- semantic metrics layer.

**Minimum implementacyjne:**
- eksport CSV i API raportowe,
- KPI: conversion, outbid rate, dispute rate, payment success,
- dashboard operacyjny + alerty.

## 3) Rekomendowana kolejność wdrożenia (pragmatyczna)

1. **Event-Driven + Outbox + Audit Journal** (fundament pod resztę).  
2. **Saga dla post-auction settlement (payment timeout + kompensacje)**.  
3. **Proxy Bidding Engine + Anulowanie z politykami**.  
4. **Discovery + Rating/Reputation**.  
5. **Reklamacje (Case Management) + Reporting + Geolokacja**.

## 4) Źródła „nowszych” archetypów (przegląd 2024–2026)

1. AWS Prescriptive Guidance — **Saga pattern**:  
   https://docs.aws.amazon.com/prescriptive-guidance/latest/modernization-data-persistence/saga-pattern.html
2. AWS Prescriptive Guidance — **Transactional outbox pattern**:  
   https://docs.aws.amazon.com/prescriptive-guidance/latest/cloud-design-patterns/transactional-outbox.html
3. AWS — **Event-Driven Architecture** (przykłady e-commerce eventów):  
   https://aws.amazon.com/event-driven-architecture/
4. Stripe Docs — **Payment Intents API** (lifecycle płatności + idempotency guidance):  
   https://docs.stripe.com/payments/payment-intents
5. Stripe Docs — **Webhook delivery/retry characteristics**:  
   https://docs.stripe.com/workbench/webhooks
6. Stripe Docs — **Cross-processor retries with Orchestration**:  
   https://docs.stripe.com/payments/orchestration/retries
7. Google Cloud — **Vertex AI Search for Commerce / recommendations models**:  
   https://cloud.google.com/solutions/retail-product-discovery  
   https://docs.cloud.google.com/retail/docs/models
8. eBay Developers — **Feedback/Rating APIs** (reputation archetype):  
   https://developer.ebay.com/api-docs/commerce/feedback/overview.html
9. MACH / Composable commerce (aktualne materiały vendorowe):  
   https://commercetools.com/assets/resources/resources-assets/wp-a-simple-guide-to-modern-commerce-terminology-en.pdf
