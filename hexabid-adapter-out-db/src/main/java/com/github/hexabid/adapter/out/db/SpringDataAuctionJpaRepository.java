package com.github.hexabid.adapter.out.db;

import com.github.hexabid.core.auctioning.model.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SpringDataAuctionJpaRepository extends JpaRepository<AuctionJpaEntity, UUID> {
    List<AuctionJpaEntity> findByStatusAndEndsAtLessThanEqual(AuctionStatus status, Instant endsAt);
}
