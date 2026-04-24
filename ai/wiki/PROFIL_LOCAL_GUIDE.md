# Hexabid - Profil `local` (Nowy System)

## 🎯 Cel

Uproszczenie uruchomienia aplikacji z lokalnym profilem development. Zamiast wielu flag Maven:
```bash
# Stary sposób (❌ zawiły)
mvn -Pkyc-local,auth-local,payment-local clean install
```

Używasz teraz prostego polecenia:
```bash
# Nowy sposób (✅ prosty)
mvn clean install  # Profil 'local' aktywny domyślnie
```

---

## 🏗️ Architektura Profili

### Maven Profiles (Dobór komponentów)

| Profil | Opis | Adaptery | Aktywny domyślnie |
|--------|------|----------|------------------|
| **local** | Development + wszystkie lokalne adaptery | local-auth, local-kyc, local-payment | ✅ Tak |
| **prod** | Production - prawdziwe serwisy | kyc (prod), payment-payu | ❌ Nie |
| local-auth | Adapter logowania lokalnego | local-auth | — |
| local-kyc | Lokalna weryfikacja KYC (mock) | local-kyc | — |
| local-payment | Lokalny mock płatności | local-payment | — |
| kyc-prod | Produkcyjna weryfikacja KYC | kyc-prod | — |

### Spring Profiles (Konfiguracja środowiska)

| Profil | Port | Context Path | Seed Data | CORS | Plik konfiguracji |
|--------|------|--------------|-----------|------|-------------------|
| `local` | 18080 | `/hexabid` | ✅ Włączone | http://localhost:14200 | `application-local.yaml` |
| `dev` | 18080 | `/` | ❌ Wyłączone | — | `application.yaml` |
| `prod` | — | — | ❌ Wyłączone | — | `application.yaml` |

---

## 🚀 Sposoby Uruchomienia

### Opcja 1: Development (Rekomendowane)
```bash
# Maven + Spring profile
mvn -f hexabid-bootstrap/pom.xml spring-boot:run \
  --spring-boot.run.arguments="--spring.profiles.active=local"

# Lub krócej:
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Rezultat**:
- Profil Maven: **local** (domyślny) → local-auth, local-kyc, local-payment
- Profil Spring: **local** → port 18080, context `/hexabid`, seed data włączone
- Baza danych: H2 w pamięci
- CORS: http://localhost:14200

### Opcja 2: Production JAR
```bash
# 1. Build z profilem prod
mvn -Pprod clean install

# 2. Uruchomienie
java -jar hexabid-bootstrap-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

### Opcja 3: Tylko Spring Profile (bez Maven profilu)
```bash
# JAR jest już zbudowany z profilem local
java -jar hexabid-bootstrap-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=local
```

---

## 📋 Checklist Uruchomienia

```bash
# ✅ Krok 1: Build (local profile domyślny)
mvn clean install

# ✅ Krok 2: Uruchomienie z profilem local
java -jar ./hexabid-bootstrap/target/hexabid-bootstrap-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=local
```

**Oczekiwane loggi**:
```
2026-04-16 21:09:58.213 INFO  [main] o.s.boot.tomcat.TomcatWebServer - 
  Tomcat started on port 18080 (http) with context path '/hexabid'
2026-04-16 21:09:58.227 INFO  [main] c.g.h.bootstrap.HexabidApplication - 
  Started HexabidApplication in 8.281 seconds
```

---

## 🔧 Konfiguracja Profili

### Maven: hexabid-bootstrap/pom.xml

```xml
<profiles>
  <!-- Local: wszystkie lokalne adaptery (domyślny) -->
  <profile>
    <id>local</id>
    <activation>
      <activeByDefault>true</activeByDefault>
    </activation>
    <dependencies>
      <dependency>hexabid-adapter-in-auth-local</dependency>
      <dependency>hexabid-adapter-out-kyc-local</dependency>
      <dependency>hexabid-payment-adapter-local</dependency>
    </dependencies>
  </profile>

  <!-- Prod: prawdziwe serwisy -->
  <profile>
    <id>prod</id>
    <dependencies>
      <dependency>hexabid-adapter-out-kyc</dependency>
      <dependency>hexabid-payment-adapter-payu</dependency>
    </dependencies>
  </profile>
</profiles>
```

### Spring: src/main/resources/application-local.yaml

```yaml
server:
  port: 18080
  servlet:
    context-path: /hexabid

auctions:
  seed:
    enabled: true
```

---

## 🎨 Przykładowe Scenariusze

### Scenariusz 1: Lokalny Development

```bash
# Build ze wszystkimi lokalnymi adapterami
mvn clean install

# Uruchomienie z profilem local
java -jar hexabid-bootstrap-0.0.1-SNAPSHOT.jar --spring.profiles.active=local

# Dostęp: http://localhost:18080/hexabid
# Z seed data (demo auctions)
```

### Scenariusz 2: Production Build

```bash
# Build z prawdziwymi serwisami
mvn -Pprod clean install

# Uruchomienie
java -jar hexabid-bootstrap-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# Dostęp: http://localhost:18080/
# Bez seed data, KYC + PayU
```

### Scenariusz 3: CI/CD Pipeline

```bash
# Build domyślny (local profil) dla testów
mvn clean test

# Build produkcji (prod profil) dla deployment
mvn -Pprod clean package
```

---

## ⚡ Skróty

| Komenda | Znaczenie |
|---------|-----------|
| `mvn clean install` | Build ze wszystkimi lokalnymi adapterami (local) |
| `mvn -Pprod clean install` | Build z adapterami produkcji |
| `mvn spring-boot:run -Dspring-boot.run.profiles=local` | Uruchomienie z profilu lokalnego |
| `java -jar app.jar --spring.profiles.active=local` | Uruchomienie JARa z profilem local |

---

## 📍 Lokalizacje

- **Profil Maven local**: `hexabid-bootstrap/pom.xml` (linie 130-160)
- **Spring config**: `hexabid-bootstrap/src/main/resources/application-local.yaml`
- **Build target**: `hexabid-bootstrap/target/hexabid-bootstrap-0.0.1-SNAPSHOT.jar`

---

## 🖥️ Systemd User Services

Aplikację można uruchamiać jako serwis systemd w przestrzeni użytkownika (bez sudo).

### Lokalizacje plików serwisów

| Serwis | Plik | Opis |
|--------|------|------|
| `hexabid-backend` | `~/.config/systemd/user/hexabid-backend.service` | Spring Boot backend (port 18080) |
| `hexabid-spa` | `~/.config/systemd/user/hexabid-spa.service` | Angular SPA (port 14200) |

### Komendy zarządzania

```bash
# Daemon reload po zmianie plików .service
systemctl --user daemon-reload

# Uruchomienie
systemctl --user start hexabid-backend
systemctl --user start hexabid-spa

# Zatrzymanie
systemctl --user stop hexabid-backend
systemctl --user stop hexabid-spa

# Restart (po rebildzie)
systemctl --user restart hexabid-backend

# Status
systemctl --user status hexabid-backend
systemctl --user status hexabid-spa

# Logi
journalctl --user -u hexabid-backend -f
journalctl --user -u hexabid-spa -f

# Autostart przy logowaniu
systemctl --user enable hexabid-backend
systemctl --user enable hexabid-spa
```

### hexabid-backend.service

```ini
[Unit]
Description=Hexabid Backend (Spring Boot)
After=network.target

[Service]
Type=simple
WorkingDirectory=/work/projects/github.com/michal-kudla/hexabid
ExecStart=java -jar hexabid-bootstrap/target/hexabid-bootstrap-0.0.1-SNAPSHOT.jar --server.port=18080 --server.servlet.context-path=/hexabid --spring.profiles.active=dev
Restart=no
Environment=SPRING_OUTPUT_ANSI_ENABLED=NEVER

[Install]
WantedBy=default.target
```

### hexabid-spa.service

```ini
[Unit]
Description=Hexabid SPA (Angular)
After=network.target

[Service]
Type=simple
WorkingDirectory=/work/projects/github.com/michal-kudla/hexabid/hexabid-spa
ExecStart=/usr/bin/npm start
Restart=no
Environment=NO_COLOR=1

[Install]
WantedBy=default.target
```

### Tip: Odtworzenie serwisów

Jeśli pliki serwisowe nie istnieją, utwórz katalog i pliki:

```bash
mkdir -p ~/.config/systemd/user
# Wklej zawartość serwisów (patrz wyżej) do:
# ~/.config/systemd/user/hexabid-backend.service
# ~/.config/systemd/user/hexabid-spa.service
systemctl --user daemon-reload
```

---

**Status: ✅ Gotowe do użytku**

Teraz możesz uruchomić aplikację prostem poleceniem:
```bash
java -jar hexabid-bootstrap-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```
