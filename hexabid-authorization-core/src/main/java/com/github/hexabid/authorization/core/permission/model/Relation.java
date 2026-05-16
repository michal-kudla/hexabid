package com.github.hexabid.authorization.core.permission.model;

/**
 * Relacja użytkownika do zasobu.
 * Określa, w jakim kontekście użytkownik może wykonać akcję.
 */
public enum Relation {
    /** Własny zasób (createdBy == userId) */
    OWN,

    /** Bezpośredni podwładny (user_supervision) */
    DIRECT_SUBORDINATE,

    /** Poddrzewo organizacyjne (organisationCode prefix match) */
    ORG_SUBTREE,

    /** Wszystkie zasoby (brak ograniczeń relacyjnych) */
    ALL
}
