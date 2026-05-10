package com.github.hexabid.statement.port.out;

import com.github.hexabid.statement.model.AuctionId;
import com.github.hexabid.statement.model.PartyId;
import com.github.hexabid.statement.model.StatementProgramInstance;
import com.github.hexabid.statement.model.StatementProgramInstanceId;

import java.util.Optional;

/**
 * Outbound port for persisting and retrieving {@link StatementProgramInstance} aggregates.
 *
 * <p>Implementations are provided by the outbound persistence adapter and must
 * guarantee that saved instances are fully rehydratable via {@link #findById}.
 */
public interface StatementProgramInstanceRepository {

    /**
     * Persists a program instance, creating or updating as needed.
     *
     * @param instance the program instance to persist
     * @return the persisted instance
     */
    StatementProgramInstance save(StatementProgramInstance instance);

    /**
     * Finds a program instance by the auction and candidate it belongs to.
     *
     * @param auctionId   the auction identifier
     * @param candidateId the candidate identifier
     * @return the matching program instance, or empty if not found
     */
    Optional<StatementProgramInstance> findByAuctionIdAndCandidateId(AuctionId auctionId, PartyId candidateId);

    /**
     * Finds a program instance by its unique identifier.
     *
     * @param id the program instance identifier
     * @return the matching program instance, or empty if not found
     */
    Optional<StatementProgramInstance> findById(StatementProgramInstanceId id);
}
