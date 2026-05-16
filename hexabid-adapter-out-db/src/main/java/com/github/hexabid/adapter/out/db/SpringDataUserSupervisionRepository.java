package com.github.hexabid.adapter.out.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repozytorium dla relacji manager-podwładny.
 */
@Repository
public interface SpringDataUserSupervisionRepository extends JpaRepository<UserSupervisionJpaEntity, UserSupervisionId> {

    List<UserSupervisionJpaEntity> findByManagerUserId(String managerUserId);

    List<UserSupervisionJpaEntity> findBySubordinateUserId(String subordinateUserId);

    @Query("SELECT COUNT(us) > 0 FROM UserSupervisionJpaEntity us WHERE us.managerUserId = :managerId AND us.subordinateUserId = :subordinateId")
    boolean existsByManagerUserIdAndSubordinateUserId(@Param("managerId") String managerUserId, @Param("subordinateId") String subordinateUserId);
}
