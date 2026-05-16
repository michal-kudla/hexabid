# Plan Finalny: Autoryzacja przez JWT, Permissions i Authorized Query

## Tytuł

Plan wdrożenia autoryzacji RBAC + Scope przez authorized query

## Podsumowanie (TLDR)

Wdrażamy jeden finalny model autoryzacji. JWT niesie `sub`, `roles`, `organisationCode`. Aplikacja mapuje role na `Permission(ResourceType, Action, Relation)`. Zasoby aukcyjne przechowuja `createdByUserId` i `createdByOrganisationCode`. Dostep do konkretnego zasobu jest sprawdzany w DB przez authorized query, a reguly domenowe pozostaja poza permissionami.

## Tagi

#authorization #rbac #scope #jwt #organisation-code #authorized-query #implementation-plan #final

## Tresc

### Zasady

- Nie rob autoryzacji zasobu wylacznie na podstawie JWT.
- Nie zapisuj listy wszystkich permissionow ani zasobow w JWT.
- Nie koduj relacji manager-podwladny w tokenie.
- Nie tworz permission explosion.
- Nie pobieraj zasobu z DB bez warunku autoryzacyjnego, jesli operacja wymaga ochrony.
- Traktuj `organisationCode` jako kod hierarchii wspierajacy szybkie zapytania, nie jako jedyne zrodlo prawdy.

### Faza 1: authorization-core

Dodaj modul:

```text
hexabid-authorization-core
```

Modele:

```text
principal/model/PrincipalContext.java
permission/model/Permission.java
permission/model/ResourceType.java
permission/model/Action.java
permission/model/Relation.java
permission/model/RolePermissionCatalog.java
context/model/AuthorizationContext.java
context/usecase/AuthorizationContextFactory.java
scope/model/OrganisationCode.java
```

Minimalne enumy:

```java
public enum ResourceType {
    AUCTION,
    REPORT
}
```

```java
public enum Action {
    READ,
    CREATE,
    EDIT,
    DELETE,
    APPROVE
}
```

```java
public enum Relation {
    OWN,
    DIRECT_SUBORDINATE,
    ORG_SUBTREE,
    ALL
}
```

`OrganisationCode`:

```java
public record OrganisationCode(String value) {
    public OrganisationCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank() || value.contains("//")) {
            throw new IllegalArgumentException("organisation code must be canonical");
        }
    }

    public boolean isSameOrBelow(OrganisationCode child) {
        return child.value.equals(value) || child.value.startsWith(value + "/");
    }
}
```

Testy:

- `sameCodeIsSameOrBelow`.
- `childCodeIsBelowParent`.
- `similarPrefixIsNotBelowParent`, np. `A12/B040` nie pasuje do `A12/B04`.
- `authorRoleMapsToOwnReadAndEdit`.
- `managerRoleMapsToDirectSubordinateAndOrgRead`.
- `unknownRoleMapsToEmptyPermissions`.
- `authorizationContextHasAnyPermission`.

### Faza 2: adapter Spring/JWT

W adapterze auth albo nowym adapterze authz dodaj:

```text
PrincipalContextFactory
JwtPrincipalContextMapper
SpringAuthenticationPrincipalContextProvider
```

Mapowanie:

```text
JWT sub              -> PrincipalContext.userId
JWT roles            -> PrincipalContext.roles
JWT organisationCode -> PrincipalContext.organisationCode
```

W profilu local/dev dodaj uzytkownikow:

```text
anna:
  sub: dev:anna
  roles: [AUCTION_AUTHOR]
  organisationCode: A12/B04/C77

marek:
  sub: dev:marek
  roles: [AUCTION_AUTHOR]
  organisationCode: A12/B04/C77

piotr:
  sub: dev:piotr
  roles: [AUCTION_MANAGER]
  organisationCode: A12/B04

barbara:
  sub: dev:barbara
  roles: [REPORT_VIEWER]
  organisationCode: A12

admin:
  sub: dev:admin
  roles: [AUCTION_ADMIN]
  organisationCode: A12
```

### Faza 3: model aukcji i DB

Rozszerz model aukcji/ogloszenia aukcyjnego o snapshot autora:

```java
PartyId createdByUserId;
OrganisationCode createdByOrganisationCode;
```

W obecnym modelu `sellerId` moze pozostac jako biznesowa nazwa ownera, ale warstwa autoryzacji powinna miec jawne pola:

```text
created_by_user_id
created_by_organisation_code
```

Migracja DB:

```sql
ALTER TABLE auction ADD COLUMN created_by_user_id VARCHAR(64);
ALTER TABLE auction ADD COLUMN created_by_organisation_code VARCHAR(256);
```

Dla istniejacych danych lokalnych wypelnij:

```text
created_by_user_id = seller_id
created_by_organisation_code = domyslny kod seed, np. A12/B04/C77
```

Docelowe indeksy:

```sql
CREATE INDEX idx_auction_created_by_user
    ON auction (created_by_user_id);

CREATE INDEX idx_auction_created_by_org
    ON auction (created_by_organisation_code);

CREATE INDEX idx_auction_id_created_by_user
    ON auction (id, created_by_user_id);

CREATE INDEX idx_auction_id_created_by_org
    ON auction (id, created_by_organisation_code);
```

### Faza 4: user_supervision

Dodaj minimalna relacje manager-podwladny:

```sql
CREATE TABLE user_supervision (
    manager_user_id VARCHAR(64) NOT NULL,
    subordinate_user_id VARCHAR(64) NOT NULL,

    PRIMARY KEY (manager_user_id, subordinate_user_id)
);
```

Indeksy:

```sql
CREATE UNIQUE INDEX idx_user_supervision_manager_subordinate
    ON user_supervision (manager_user_id, subordinate_user_id);

CREATE INDEX idx_user_supervision_subordinate_manager
    ON user_supervision (subordinate_user_id, manager_user_id);
```

Seed local:

```text
piotr -> anna
piotr -> marek
```

Closure table zostaw na pozniej, gdy pojawi sie potrzeba dalszych podwladnych.

### Faza 5: authorized repository queries

Dodaj metody repozytorium dla operacji chronionych:

```java
Optional<Auction> findAuthorizedForEdit(
        AuctionId auctionId,
        AuthorizationContext auth
);
```

SQL:

```sql
SELECT a.*
FROM auction a
WHERE a.id = :auction_id
  AND (
        (:can_edit_own = TRUE
            AND a.created_by_user_id = :current_user_id)

        OR (:can_edit_direct_subordinate = TRUE
            AND EXISTS (
                SELECT 1
                FROM user_supervision us
                WHERE us.manager_user_id = :current_user_id
                  AND us.subordinate_user_id = a.created_by_user_id
            ))

        OR (:can_edit_org_subtree = TRUE
            AND (
                a.created_by_organisation_code = :current_organisation_code
                OR a.created_by_organisation_code LIKE CONCAT(:current_organisation_code, '/%')
            ))

        OR (:can_edit_all = TRUE)
  );
```

Parametry boolowskie wyliczaj z `AuthorizationContext`:

```java
boolean canEditOwn = auth.hasPermission(AUCTION, EDIT, OWN);
boolean canEditDirectSubordinate = auth.hasPermission(AUCTION, EDIT, DIRECT_SUBORDINATE);
boolean canEditOrgSubtree = auth.hasPermission(AUCTION, EDIT, ORG_SUBTREE);
boolean canEditAll = auth.hasPermission(AUCTION, EDIT, ALL);
```

Analogicznie dodaj:

```text
findAuthorizedForRead
findAuthorizedForDelete
findAuthorizedForApprove
```

### Faza 6: use case/service layer

Flow dla edycji:

```java
public Auction editAuction(AuctionId auctionId, EditAuctionCommand command) {
    AuthorizationContext auth = authorizationContextFactory.current();

    if (!auth.hasAnyPermission(ResourceType.AUCTION, Action.EDIT)) {
        throw new AccessDeniedException("Missing permission for auction edit");
    }

    Auction auction = auctionRepository.findAuthorizedForEdit(auctionId, auth)
            .orElseThrow(() -> new AccessDeniedException("Auction not accessible"));

    if (auction.status() == AuctionStatus.PUBLISHED) {
        throw new AccessDeniedException("Published auction cannot be edited");
    }

    return auctionRepository.update(auctionId, command);
}
```

Reguly domenowe pozostaja po pobraniu autoryzowanym albo w domenowym use case, np.:

- nie edytuj opublikowanej aukcji,
- nie zatwierdzaj wlasnej aukcji,
- nie usuwaj zaakceptowanej aukcji.

Nie zamieniaj tych regul na nowe permissions.

### Faza 7: REST adapter

Endpointy mapuja operacje na `ResourceType + Action`:

| Endpoint | ResourceType | Action |
|----------|--------------|--------|
| `GET /api/auctions/{id}` | `AUCTION` | `READ` |
| `POST /api/auctions` | `AUCTION` | `CREATE` |
| `PUT /api/auctions/{id}` | `AUCTION` | `EDIT` |
| `DELETE /api/auctions/{id}` | `AUCTION` | `DELETE` |
| `POST /api/auctions/{id}/activate` | `AUCTION` | `APPROVE` |
| `GET /api/reports/...` | `REPORT` | `READ` |

Tworzenie aukcji:

- sprawdz `hasAnyPermission(AUCTION, CREATE)`,
- zapisz `createdByUserId = principal.userId`,
- zapisz `createdByOrganisationCode = principal.organisationCode`.

Edycja i odczyt po ID:

- uzyj authorized query,
- dla braku wyniku zwroc `404` albo `403` zgodnie z przyjeta polityka.

### Faza 8: listy i browse

Dla list aukcji nie pobieraj wszystkiego. Buduj query z warunkami:

- `OWN` -> `created_by_user_id = :current_user_id`,
- `DIRECT_SUBORDINATE` -> `EXISTS user_supervision`,
- `ORG_SUBTREE` -> `created_by_organisation_code = :code OR LIKE :code/%`,
- `ALL` -> brak ograniczenia relacyjnego.

Dodaj testy na brak wycieku aukcji z podobnym prefiksem:

```text
A12/B04 widzi A12/B04/C77
A12/B04 nie widzi A12/B040/C77
```

### Faza 9: ArchUnit

Reguly:

- `hexabid-authorization-core` nie zalezy od Springa.
- `hexabid-authorization-core` nie zalezy od JPA/Hibernate.
- Adapter REST moze zalezec od portow autoryzacji, nie od szczegolow JWT.
- Domena aukcji nie importuje Spring Security.

### Faza 10: audyt i diagnostyka

Loguj decyzje:

```text
userId
roles
organisationCode
resourceType
resourceId
action
permissions
decision
matchedRelation albo reason
```

Dla dev/local mozna dodac endpoint:

```text
GET /api/authz/me
```

Zwraca:

```text
userId
roles
organisationCode
effectivePermissions
```

Nie dodawaj endpointu, ktory zdradza cudze zasoby bez authorized query.

### Faza 11: SPA

SPA uzywa autoryzacji tylko do UX, nie do bezpieczenstwa:

- pokazuje role i `organisationCode` w profilu dev,
- ukrywa przyciski edycji, gdy backend zwroci brak dostepu,
- obsluguje `403` i `404`,
- nie zaklada dostepu tylko na podstawie JWT.

E2E:

- Anna edytuje wlasna aukcje.
- Anna widzi wlasna aukcje, ale nie edytuje aukcji Marka, jesli ma tylko `OWN`.
- Piotr edytuje aukcje Anny przez `DIRECT_SUBORDINATE`.
- Uzytkownik z `A12/B04` nie widzi zasobu `A12/B040`.
- Report viewer widzi raporty, ale nie edytuje aukcji.

### Kolejnosc PR

1. `hexabid-authorization-core`: modele, katalog rol, testy.
2. JWT/Spring principal context i dev users.
3. Pola `created_by_user_id`, `created_by_organisation_code` w aukcjach i seed data.
4. `user_supervision` i seed relacji.
5. Authorized queries dla read/edit/delete/approve.
6. Integracja REST/use case.
7. Listy aukcji z filtrem autoryzacyjnym.
8. Audyt, endpoint dev, SPA i E2E.

### Linki

- [[../decisions/2026-05-15-authorization-rbac-scope-final.md]] - finalny ADR.
- [[../log.md]] - log zmian.
