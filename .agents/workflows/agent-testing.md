---
description: Procedura uruchamiania i testowania środowiska (E2E, API)
---

# Procedura testowania aplikacji Hexabid

Ten dokument zarządza sposobem, w jaki Agenci AI powinni uruchamiać, weryfikować i testować aplikację Hexabid, by odzwierciedlić jak najdokładniej perspektywę użytkownika.

## Uruchamianie lokalnego środowiska
Aplikacja z zasady składa się z kooperującego po sieci backendu (Spring Boot) i frontendu (SPA Angular/React). Do sprawnego testowania zjawisk typu wejście-wyjście, niezbędne jest uniesienie obu części:
1. **Backend:** Wykorzystaj terminal i wpisz `mvn spring-boot:run -f auctions-bootstrap/pom.xml -Dspring-boot.run.profiles=dev`, aby uruchomić warstwę usług wraz z testową bazą w pamięci (H2).
2. **Frontend:** Przejdź poleceniem do folderu `hexabid-spa` i uruchom `npm install` a potem `npm start`.

## Podejście do weryfikacji End-to-End (Front + Back)
Po implementacji nowych funkcji, główną ścieżkę walidacyjną należy realizować przez wbudowany interfejs webowy (frontend).
- **Subagent Przeglądarkowy:** Agencie główny, o ile masz dostępne narzędzie przeglądarki (np. `browser_subagent`), deleguj do niego zadanie: `"otwórz localhost:X, zaloguj w aplikacji, przejdź przez proces dodania przedmiotu, kliknij element Y"`. Weryfikuj wykonane kroki na podstawie zrzutów z przeglądarki.
- **Automatyzacja E2E:** Możesz używać narzędzia takiego jak **Playwright** - samodzielnie generować jego skrypty testowe dla konkretnego case'u, a następnie uruchamiać je z konsoli poleceniem Node. Jest to potężny atut w przypadku przechwytywania żądań WebSocket/STOMP.

## Testy składowych elementów backendu (HTTP & WebSockets)
- Stosuj pliki `.http` uruchamiane prosto z edytora IDE bądź przez wbudowanego klienta `curl` w skryptach testowych dla walidacji odpowiedzi z REST.
- W przypadku ręcznej kontroli lub izolowanych walidacji logiki asynchronicznej weź pod uwagę tworzenie skryptów STOMP (odpalających wiadomości jako symulacja klienta WS) z poziomu języka, do którego masz największą pewność generowania (najlepiej prosty skrypt Node + `ws` + `@stomp/stompjs`).
