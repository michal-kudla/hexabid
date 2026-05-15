# ADR: Autoryzacja RBAC + Scope przez Authorized Query

## Tytuł

Autoryzacja RBAC + Scope przez JWT, `organisationCode` i authorized query

## Podsumowanie (TLDR)

Autoryzacja w Hexabid nie bedzie wykonywana wylacznie na podstawie JWT. JWT daje szybki kontekst aktora: `sub`, `roles`, `organisationCode`. Role sa mapowane w aplikacji na potencjalne `Permission(ResourceType, Action, Relation)`, bez eksplozji plaskich nazw uprawnien. Ostateczna decyzja dostepu do konkretnego zasobu powstaje w bazie przez **authorized query**, czyli pobranie zasobu od razu z warunkiem autoryzacyjnym. `organisationCode` jest kanonicznym kodem hierarchii, zapisywanym takze przy zasobie, aby warunki organizacyjne byly szybkie i indeksowalne.

## Tagi

#authorization #rbac #scope #jwt #organisation-code #authorized-query #database #permission-model #final

## Tresc

### Status

Finalna decyzja projektowa. Ten dokument jest jedyna aktualna wersja ADR dla autoryzacji RBAC + scope. Poprzednie warianty zostaly usuniete z wiki w ramach czyszczenia.

### Decyzja architektoniczna

Przyjmujemy model:

```text
JWT + role -> potencjalne permissions
DB + dane zasobu + relacje -> faktyczna decyzja dostepu
```

Nie eliminujemy odczytu z bazy. Projektujemy dane, indeksy i zapytania tak, zeby sprawdzenie autoryzacji bylo tanie, przewidywalne i wykonane razem z pobraniem zasobu.

### JWT

Token niesie minimalny stabilny kontekst:

```json
{
  "sub": "user-123",
  "roles": ["AUCTION_MANAGER"],
  "organisationCode": "A12/B04/C77"
}
```

Znaczenie pol:

- `sub` - identyfikator uzytkownika, wymagany dla relacji `OWN`.
- `roles` - role biznesowe, z ktorych aplikacja wylicza potencjalne permissions.
- `organisationCode` - zakodowane miejsce uzytkownika w hierarchii organizacyjnej.

JWT nie niesie:

- pelnej listy permissionow,
- listy dostepnych zasobow,
- ownerow zasobow,
- relacji manager-podwladny,
- pelnej listy organizacji.

Relacje zasobowe i personalne zostaja w bazie.

### OrganisationCode

`organisationCode` jest kanonicznym path code:

```text
A12/B04/C77
```

Wymagania:

- kanoniczny,
- jednoznaczny,
- stabilny,
- walidowany przez system wystawiajacy token,
- porownywalny przez operacje prefiksowe z granica segmentu.

Poprawne sprawdzenie poddrzewa:

```java
public boolean isSameOrBelow(String parentCode, String childCode) {
    return childCode.equals(parentCode)
            || childCode.startsWith(parentCode + "/");
}
```

Nie uzywamy surowego `startsWith(parentCode)` bez separatora, bo `A12/B04` nie moze pasowac do `A12/B040`.

### Model permissionow

Unikamy permission explosion. Permission sklada sie z trzech niezaleznych wymiarow:

```java
public record Permission(
        ResourceType resourceType,
        Action action,
        Relation relation
) {
}
```

Przykladowe enumy dla fazy 1:

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

Nie tworzymy setek nazw:

```text
AUCTION_OWN_EDIT
AUCTION_DIRECT_SUBORDINATE_EDIT
AUCTION_ORG_SUBTREE_EDIT
AUCTION_ALL_EDIT
```

Zamiast tego mamy:

```text
AUCTION + EDIT + OWN
AUCTION + EDIT + DIRECT_SUBORDINATE
AUCTION + EDIT + ORG_SUBTREE
AUCTION + EDIT + ALL
```

### Role

Role sa biznesowe i moga byc wielokrotne:

```text
AUCTION_AUTHOR
AUCTION_MANAGER
AUCTION_REGIONAL_READER
AUCTION_ADMIN
REPORT_VIEWER
```

Role mapujemy w aplikacji na permissions:

```java
public final class RolePermissionCatalog {

    public static Set<Permission> permissionsFor(String role) {
        return switch (role) {
            case "AUCTION_AUTHOR" -> Set.of(
                    new Permission(ResourceType.AUCTION, Action.READ, Relation.OWN),
                    new Permission(ResourceType.AUCTION, Action.EDIT, Relation.OWN)
            );

            case "AUCTION_MANAGER" -> Set.of(
                    new Permission(ResourceType.AUCTION, Action.READ, Relation.DIRECT_SUBORDINATE),
                    new Permission(ResourceType.AUCTION, Action.EDIT, Relation.DIRECT_SUBORDINATE),
                    new Permission(ResourceType.AUCTION, Action.READ, Relation.ORG_SUBTREE)
            );

            case "AUCTION_ADMIN" -> Set.of(
                    new Permission(ResourceType.AUCTION, Action.READ, Relation.ALL),
                    new Permission(ResourceType.AUCTION, Action.EDIT, Relation.ALL),
                    new Permission(ResourceType.AUCTION, Action.DELETE, Relation.ALL)
            );

            case "REPORT_VIEWER" -> Set.of(
                    new Permission(ResourceType.REPORT, Action.READ, Relation.ORG_SUBTREE)
            );

            default -> Set.of();
        };
    }

    private RolePermissionCatalog() {
    }
}
```

### Spring Security

Springowe `GrantedAuthority` moze pozostac technicznym mechanizmem coarse-grained security, ale nie jest glownym modelem domenowym autoryzacji zasobu.

Rekomendacja:

- `ROLE_USER`, `ROLE_ADMIN` - tylko jesli potrzebne do ogolnej ochrony endpointow.
- Role biznesowe z JWT mapuj do `PrincipalContext.roles`.
- Permissions wyliczaj w aplikacji z `RolePermissionCatalog`.
- Nie koduj calego modelu `resource + action + relation` w nazwach Spring authorities.

### Model zasobu

Dla aukcji/ogloszenia aukcyjnego zasob musi przechowywac atrybuty autoryzacyjne:

```sql
CREATE TABLE auction (
    id UUID PRIMARY KEY,

    created_by_user_id VARCHAR(64) NOT NULL,
    created_by_organisation_code VARCHAR(256) NOT NULL,

    status VARCHAR(32) NOT NULL,

    title VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

Znaczenie:

- `created_by_user_id` - sprawdzenie relacji `OWN`.
- `created_by_organisation_code` - sprawdzenie `ORG_SUBTREE`.
- `status` - reguly domenowe, np. brak edycji opublikowanej aukcji.

Na start nie dodajemy osobnej `owner_organisation`. Wystarcza snapshot organizacji autora zasobu.

### Relacja manager-podwladny

Relacja personalna zostaje w DB:

```sql
CREATE TABLE user_supervision (
    manager_user_id VARCHAR(64) NOT NULL,
    subordinate_user_id VARCHAR(64) NOT NULL,

    PRIMARY KEY (manager_user_id, subordinate_user_id)
);
```

Indeks odwrotny:

```sql
CREATE INDEX idx_user_supervision_subordinate_manager
    ON user_supervision (subordinate_user_id, manager_user_id);
```

Dla dalszego drzewa podwladnych mozna pozniej dodac closure table:

```sql
CREATE TABLE user_supervision_closure (
    manager_user_id VARCHAR(64) NOT NULL,
    subordinate_user_id VARCHAR(64) NOT NULL,
    depth INT NOT NULL,

    PRIMARY KEY (manager_user_id, subordinate_user_id)
);
```

### Centralny wzorzec: authorized query

Nie rekomendujemy modelu:

```text
pobierz zasob z DB
sprawdz w Javie, czy uzytkownik ma dostep
```

Rekomendujemy:

```text
pobierz zasob z DB juz z warunkiem autoryzacyjnym
```

Brak wyniku oznacza brak zasobu albo brak dostepu.

Przyklad dla edycji aukcji:

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

To jest glowny mechanizm autoryzacji zasobu.

### Indeksy

Minimalne indeksy:

```sql
CREATE INDEX idx_auction_created_by_user
    ON auction (created_by_user_id);

CREATE INDEX idx_auction_created_by_org
    ON auction (created_by_organisation_code);

CREATE INDEX idx_auction_id_created_by_user
    ON auction (id, created_by_user_id);

CREATE INDEX idx_auction_id_created_by_org
    ON auction (id, created_by_organisation_code);

CREATE UNIQUE INDEX idx_user_supervision_manager_subordinate
    ON user_supervision (manager_user_id, subordinate_user_id);

CREATE INDEX idx_user_supervision_subordinate_manager
    ON user_supervision (subordinate_user_id, manager_user_id);
```

Dla endpointow po ID baza najpierw znajduje rekord po primary key, a potem tanio sprawdza warunki autoryzacyjne.

### Warstwa aplikacyjna

Flow:

```text
1. Request wchodzi na endpoint.
2. Spring Security waliduje JWT.
3. Z JWT powstaje PrincipalContext: userId, roles, organisationCode.
4. Z rol wyliczane sa permissions.
5. Endpoint okresla ResourceType i Action.
6. Guard sprawdza, czy istnieje jakikolwiek permission dla ResourceType + Action.
7. Repository wykonuje authorized query.
8. Jesli rekord zostal zwrocony, operacja moze byc wykonana.
9. Jesli rekord nie zostal zwrocony, zwracamy 403 albo 404 zgodnie z polityka endpointu.
```

Modele:

```java
public record PrincipalContext(
        String userId,
        Set<String> roles,
        String organisationCode
) {
}
```

```java
public record AuthorizationContext(
        PrincipalContext principal,
        Set<Permission> permissions
) {
    public boolean hasPermission(ResourceType resourceType, Action action, Relation relation) {
        return permissions.contains(new Permission(resourceType, action, relation));
    }

    public boolean hasAnyPermission(ResourceType resourceType, Action action) {
        return permissions.stream()
                .anyMatch(permission ->
                        permission.resourceType() == resourceType
                                && permission.action() == action
                );
    }
}
```

### Permission to nie regula domenowa

Permission odpowiada na pytanie:

```text
Czy uzytkownik potencjalnie moze wykonac akcje na zasobie w danej relacji?
```

Reguly domenowe sa osobnym krokiem:

```text
permission pasuje
AND relacja do zasobu pasuje
AND reguly domenowe pasuja
```

Nie tworzymy permissionow typu:

```text
AUCTION_OWN_EDIT_WHEN_DRAFT
AUCTION_SUBORDINATE_EDIT_WHEN_DRAFT
```

Zamiast tego:

```text
Permission: AUCTION + EDIT + OWN
Domain rule: status == DRAFT
```

### Cache

Cache nie jest fundamentem bezpieczenstwa. Na start wystarcza dobre query i indeksy.

Bezpieczny cache:

```text
Role -> Permissions
```

Cache wymagajacy ostroznosci:

```text
manager -> subordinates
resourceId -> authorization attributes
```

### 403 czy 404

Dla endpointow po ID:

- `404` - gdy nie chcemy ujawniac, ze zasob istnieje.
- `403` - gdy diagnostyka wewnetrzna jest wazniejsza.

Polityke nalezy ustalic per endpoint. Dla API publiczniejszego preferowane jest `404`.

### Audyt

Log decyzji powinien zawierac:

```text
userId=user-123
roles=[AUCTION_MANAGER]
organisationCode=A12/B04
resourceType=AUCTION
resourceId=...
action=EDIT
permissions=[AUCTION:EDIT:DIRECT_SUBORDINATE]
decision=ALLOW
matchedRelation=DIRECT_SUBORDINATE
```

Dla odmowy:

```text
decision=DENY
reason=NO_MATCHING_RELATION
```

### Konsekwencje

Plusy:

- JWT pozostaje maly i stabilny.
- Decyzja zasobowa korzysta z prawdziwych danych z DB.
- `organisationCode` daje szybkie sprawdzenie poddrzewa.
- Unikamy eksplozji nazw permissionow.
- Authorized query ogranicza ryzyko przypadkowego pobrania zasobu bez kontroli.

Minusy:

- Nie ma pelnej decyzji offline z samego JWT.
- Repository musi miec warianty zapytan autoryzowanych.
- Trzeba pilnowac indeksow i spojnosc `created_by_organisation_code`.

### Linki

- [[../plans/authorization-rbac-scope-final.md]] - finalny plan wdrozenia.
- [[../concepts/hexagonal-architecture.md]] - reguly architektury heksagonalnej.
