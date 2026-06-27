package com.github.hexabid.adapter.out.db;

import com.github.hexabid.authorization.core.scope.port.out.SubordinateQueryPort;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Adapter implementujący SubordinateQueryPort poprzez Spring Data JPA.
 */
@Component
public class JpaSubordinateQueryAdapter implements SubordinateQueryPort {

    private final @Nullable SpringDataUserSupervisionRepository supervisionRepository;

    public JpaSubordinateQueryAdapter(@Nullable SpringDataUserSupervisionRepository supervisionRepository) {
        this.supervisionRepository = supervisionRepository;
    }

    @Override
    public Set<String> findDirectSubordinates(String managerUserId) {
        Objects.requireNonNull(managerUserId, "managerUserId must not be null");
        if (supervisionRepository == null) {
            return Set.of();
        }
        return supervisionRepository.findByManagerUserId(managerUserId).stream()
                .map(UserSupervisionJpaEntity::getSubordinateUserId)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isDirectSubordinate(String managerUserId, String subordinateUserId) {
        Objects.requireNonNull(managerUserId, "managerUserId must not be null");
        Objects.requireNonNull(subordinateUserId, "subordinateUserId must not be null");
        if (supervisionRepository == null) {
            return false;
        }
        return supervisionRepository.existsByManagerUserIdAndSubordinateUserId(managerUserId, subordinateUserId);
    }
}
