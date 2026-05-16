package com.github.hexabid.authorization.core.scope.port.out;

import java.util.Set;

/**
 * Port wyjściowy do sprawdzania relacji przełożony-podwładny.
 * Implementacja w adapterze outbound (np. hexabid-adapter-out-db).
 */
public interface SubordinateQueryPort {

    /**
     * Zwraca identyfikatory bezpośrednich podwładnych danego przełożonego.
     */
    Set<String> findDirectSubordinates(String managerUserId);

    /**
     * Sprawdza, czy dany użytkownik jest bezpośrednim podwładnym przełożonego.
     */
    boolean isDirectSubordinate(String managerUserId, String subordinateUserId);
}
