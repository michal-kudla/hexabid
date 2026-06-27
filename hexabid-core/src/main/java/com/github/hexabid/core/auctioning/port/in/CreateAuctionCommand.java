package com.github.hexabid.core.auctioning.port.in;

import com.github.hexabid.core.auctioning.model.Price;
import com.github.hexabid.core.party.model.PartyId;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

/**
 * Komenda tworzenia aukcji.
 * Zawiera atrybuty autoryzacyjne (createdByUserId, createdByOrganisationCode)
 * które są snapshotem autora w momencie tworzenia.
 */
public record CreateAuctionCommand(
        PartyId sellerId,
        String createdByUserId,
        String createdByOrganisationCode,
        String title,
        Price startingPrice,
        Instant endsAt,
        @Nullable String participationPolicyTemplate
) {

    public CreateAuctionCommand {
        Objects.requireNonNull(sellerId, "sellerId must not be null");
        Objects.requireNonNull(createdByUserId, "createdByUserId must not be null");
        Objects.requireNonNull(createdByOrganisationCode, "createdByOrganisationCode must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(startingPrice, "startingPrice must not be null");
        Objects.requireNonNull(endsAt, "endsAt must not be null");
    }
}
