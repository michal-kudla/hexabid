package com.github.hexabid.authorization.core.scope.model;

import java.util.Objects;

/**
 * Kanoniczny kod organizacyjny reprezentujący pozycję w hierarchii.
 * Format: segmenty rozdzielone "/", np. "A12/B04/C77".
 * <p>
 * Walidacja: brak pustych segmentów, brak podwójnych separatorów.
 * Porównanie poddrzewa: {@link #isSameOrBelow(OrganisationCode)} z zachowaniem granicy segmentu.
 */
public record OrganisationCode(String value) {

    public OrganisationCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("organisation code must not be blank");
        }
        if (value.contains("//")) {
            throw new IllegalArgumentException("organisation code must not contain empty segments");
        }
        if (value.startsWith("/") || value.endsWith("/")) {
            throw new IllegalArgumentException("organisation code must not start or end with separator");
        }
    }

    /**
     * Sprawdza, czy {@code child} jest tym samym węzłem lub poddrzewem tego kodu.
     * Używa prefiksu z separatorem, aby uniknąć false positive na podobnych prefiksach.
     * <p>
     * Przykład: {@code A12/B04} pasuje do {@code A12/B04/C77}, ale nie do {@code A12/B040/C77}.
     */
    public boolean isSameOrBelow(OrganisationCode child) {
        Objects.requireNonNull(child, "child must not be null");
        return child.value.equals(this.value)
                || child.value.startsWith(this.value + "/");
    }

    @Override
    public String toString() {
        return value;
    }
}
