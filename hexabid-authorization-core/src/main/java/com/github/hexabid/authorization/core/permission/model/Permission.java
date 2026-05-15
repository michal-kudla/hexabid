package com.github.hexabid.authorization.core.permission.model;

import java.util.Objects;

/**
 * Uprawnienie do wykonania akcji na zasobie w określonej relacji.
 * Składa się z trzech niezależnych wymiarów: typ zasobu, akcja, relacja.
 * <p>
 * Unikamy eksplozji flat permission names (np. AUCTION_OWN_EDIT, AUCTION_SUBTREE_READ).
 * Zamiast tego: {@code Permission(AUCTION, EDIT, OWN)}.
 *
 * @param resourceType typ zasobu
 * @param action       operacja
 * @param relation     relacja użytkownika do zasobu
 */
public record Permission(ResourceType resourceType, Action action, Relation relation) {

    public Permission {
        Objects.requireNonNull(resourceType, "resourceType must not be null");
        Objects.requireNonNull(action, "action must not be null");
        Objects.requireNonNull(relation, "relation must not be null");
    }
}
