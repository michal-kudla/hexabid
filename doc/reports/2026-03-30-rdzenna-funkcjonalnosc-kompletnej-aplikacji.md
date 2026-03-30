# Rdzenna brakująca funkcjonalność: domknięcie cyklu transakcyjnego po wygranej aukcji

**Data:** 2026-03-30  
**Zakres:** przegląd `auctions-core` i kontraktów wejściowych pod kątem minimalnego zestawu funkcji wymaganych, aby system działał jako kompletna aplikacja aukcyjna (a nie tylko silnik licytacji).

## 1. Co już działa w rdzeniu

W obecnym rdzeniu są poprawnie zaadresowane podstawy silnika licytacji:
- tworzenie aukcji,
- składanie ofert,
- zamykanie wygasłych aukcji,
- emisja zdarzenia o wygranej lub zamknięciu bez zwycięzcy.

## 2. Kluczowa luka produktowa

Największym brakującym elementem jest **domknięcie cyklu po wygranej**: od stanu „auction won” do stanu „transakcja skutecznie rozliczona i finalnie zamknięta”.

Bez tej części system nie jest pełnoprawną aplikacją aukcyjną, bo:
1. nie ma gwarancji, że wygrana przekłada się na płatność,
2. nie ma polityki timeoutów i konsekwencji braku płatności,
3. nie ma pełnej ścieżki „success/fail/dispute/refund”,
4. nie ma finalnego statusu biznesowego transakcji, który zamyka odpowiedzialność stron.

## 3. Proponowana rdzenna funkcjonalność dodatkowa

## **Post-auction settlement orchestration**

Czyli nowy moduł/use case domenowy, który prowadzi aukcję od zdarzenia `AuctionWon` do jednego z końcowych rezultatów:
- `SETTLED` (zapłacono i potwierdzono),
- `FAILED_PAYMENT` (zwycięzca nie zapłacił / płatność odrzucona),
- `REFUNDED` (zwrot po korekcie),
- opcjonalnie `DISPUTED` (spór wymagający decyzji).

W praktyce to oznacza:
- statusy rozliczeniowe niezależne od samego `OPEN/CLOSED`,
- deadline na opłacenie po wygranej,
- zdarzenia domenowe rozliczeń,
- automatyczne akcje kompensacyjne (np. oferta dla kolejnego licytanta albo relisting).

## 4. Scenariusze, które muszą być koniecznie zaimplementowane

Poniższe scenariusze są „must-have”, aby system był kompletny i produkcyjnie używalny.

1. **Wygrana + płatność zakończona sukcesem**
   - `AuctionWon` uruchamia proces płatności.
   - Gateway potwierdza sukces.
   - Aukcja przechodzi do finalnego statusu rozliczonego (`SETTLED`).

2. **Wygrana + brak płatności w czasie (timeout)**
   - Po przekroczeniu SLA płatności zwycięstwo wygasa.
   - System uruchamia politykę kompensacji (np. oferta dla drugiego miejsca lub relisting).

3. **Wygrana + płatność odrzucona przez operatora**
   - Odrzucenie musi być jawnie zapisane w historii zdarzeń.
   - Aukcja przechodzi do `FAILED_PAYMENT` i uruchamiana jest ta sama polityka kompensacji co przy timeout.

4. **Idempotencja callbacków płatności**
   - Powtórzony callback (duplikat) nie może podwajać efektów domenowych.
   - System musi wykrywać i ignorować duplikaty po `paymentTransactionId`/`idempotencyKey`.

5. **Niespójność kwoty/waluty między aukcją a płatnością**
   - Callback z inną kwotą/walutą jest odrzucany jako naruszenie integralności.
   - Przypadek trafia do ścieżki wyjątków (manual review / dispute).

6. **Zwrot środków (refund) po błędnym rozliczeniu**
   - Refund jest odrębną operacją domenową z audytem.
   - Stan końcowy musi rozróżniać „settled” i „settled-then-refunded”.

7. **Spójność procesu przy awarii infrastruktury**
   - Jeżeli zapis stanu się powiedzie, a publikacja zdarzenia nie, system odtworzy publikację (outbox/inbox).
   - Jeśli gateway odpowie po czasie, korelacja odpowiedzi nadal poprawnie aktualizuje właściwą aukcję.

8. **Audyt i rozliczalność decyzji**
   - Dla każdej aukcji musi dać się odtworzyć pełną sekwencję: wygrana -> próby płatności -> wynik -> kompensacja.
   - Każdy krok ma identyfikator korelacyjny i znacznik czasu.

## 5. Definicja „done” dla kompletnej aplikacji aukcyjnej

Za minimum kompletności przyjmujemy, że system dostarcza:
- pełny lifecycle aukcji i transakcji (nie tylko licytacji),
- automatyczne domknięcie procesów nieopłaconych,
- integralność finansową (idempotencja + walidacja kwot/walut),
- obserwowalny audyt biznesowy (co się stało, kiedy i dlaczego).

Dopiero wtedy aplikacja spełnia oczekiwania platformy aukcyjnej end-to-end.
