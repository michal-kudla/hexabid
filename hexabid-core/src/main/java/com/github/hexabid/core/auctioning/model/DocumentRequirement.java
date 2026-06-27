package com.github.hexabid.core.auctioning.model;

import com.github.hexabid.core.auctioning.model.RulePhase;

import java.time.Instant;
import java.util.Objects;

public record DocumentRequirement(
        DocumentType type,
        DocumentStatus requiredStatus,
        RulePhase requiredByPhase,
        Instant deadline
) {

    public DocumentRequirement {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(requiredStatus, "requiredStatus must not be null");
        Objects.requireNonNull(requiredByPhase, "requiredByPhase must not be null");
    }
}
