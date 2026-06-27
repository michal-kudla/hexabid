package com.github.hexabid.adapter.out.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link StatementProgramInstanceJpaEntity}.
 *
 * <p>Provides standard CRUD operations inherited from {@link JpaRepository}
 * and a custom lookup by auction ID and candidate ID.</p>
 */
interface SpringDataStatementProgramInstanceJpaRepository extends JpaRepository<StatementProgramInstanceJpaEntity, UUID> {

    Optional<StatementProgramInstanceJpaEntity> findByAuctionIdAndCandidateId(UUID auctionId, String candidateId);
}
