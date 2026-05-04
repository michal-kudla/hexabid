package com.github.hexabid.core.auctioning.port.in;

public sealed interface SettlementResult permits SettlementResult.SettlementSucceeded, SettlementResult.SettlementFailed {

    record SettlementSucceeded() implements SettlementResult { }

    record SettlementFailed(String reason) implements SettlementResult {
        public SettlementFailed {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("reason must not be blank");
            }
        }
    }
}
