package com.acme.auctions.adapter.out.kyc;

import com.acme.auctions.core.auctioning.port.out.KycClient;
import com.acme.auctions.core.party.model.PartyId;
import com.acme.auctions.kyc.client.api.KycVerificationApi;
import com.acme.auctions.kyc.client.invoker.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class GeneratedKycClientAdapter implements KycClient {

    private static final Logger log = LoggerFactory.getLogger(GeneratedKycClientAdapter.class);

    private final KycVerificationApi kycVerificationApi;
    private final Clock clock;
    private final int maxAttempts;
    private final long initialBackoffMillis;
    private final int circuitBreakerFailureThreshold;
    private final long circuitBreakerOpenMillis;

    private int consecutiveRetryableFailures = 0;
    private Instant circuitOpenUntil;

    public GeneratedKycClientAdapter(
            KycVerificationApi kycVerificationApi,
            Clock clock,
            @Value("${auctions.kyc.retry.max-attempts:3}") int maxAttempts,
            @Value("${auctions.kyc.retry.initial-backoff-millis:200}") long initialBackoffMillis,
            @Value("${auctions.kyc.circuit-breaker.failure-threshold:5}") int circuitBreakerFailureThreshold,
            @Value("${auctions.kyc.circuit-breaker.open-millis:30000}") long circuitBreakerOpenMillis
    ) {
        this.kycVerificationApi = kycVerificationApi;
        this.clock = clock;
        this.maxAttempts = maxAttempts;
        this.initialBackoffMillis = initialBackoffMillis;
        this.circuitBreakerFailureThreshold = circuitBreakerFailureThreshold;
        this.circuitBreakerOpenMillis = circuitBreakerOpenMillis;
    }

    @Override
    public boolean isVerified(PartyId partyId) {
        if (isCircuitBreakerOpen()) {
            throw new KycClientCommunicationException(
                    KycErrorClassification.RETRYABLE,
                    "KYC circuit breaker is open; skipping external call"
            );
        }

        ApiException lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                boolean verified = kycVerificationApi.verifyActor(partyId.value()).getVerified();
                markSuccess();
                return verified;
            } catch (ApiException exception) {
                lastException = exception;
                KycErrorClassification classification = classify(exception);

                if (classification == KycErrorClassification.NON_RETRYABLE) {
                    log.warn("Non-retryable KYC error for actor {}: status={}, message={}",
                            partyId.value(), exception.getCode(), exception.getMessage());
                    throw new KycClientCommunicationException(
                            classification,
                            "KYC verification failed with non-retryable error",
                            exception
                    );
                }

                markRetryableFailure();
                if (attempt < maxAttempts) {
                    sleepQuietly(backoffFor(attempt));
                    continue;
                }
            }
        }

        throw new KycClientCommunicationException(
                KycErrorClassification.RETRYABLE,
                "KYC verification failed after retry attempts",
                lastException
        );
    }

    private synchronized boolean isCircuitBreakerOpen() {
        return circuitOpenUntil != null && clock.instant().isBefore(circuitOpenUntil);
    }

    private synchronized void markSuccess() {
        consecutiveRetryableFailures = 0;
        circuitOpenUntil = null;
    }

    private synchronized void markRetryableFailure() {
        consecutiveRetryableFailures++;
        if (consecutiveRetryableFailures >= circuitBreakerFailureThreshold) {
            circuitOpenUntil = clock.instant().plus(Duration.ofMillis(circuitBreakerOpenMillis));
            log.warn("Opening KYC circuit breaker for {} ms after {} consecutive retryable failures",
                    circuitBreakerOpenMillis, consecutiveRetryableFailures);
        }
    }

    private long backoffFor(int attempt) {
        return initialBackoffMillis * (1L << Math.max(0, attempt - 1));
    }

    private static KycErrorClassification classify(ApiException exception) {
        int code = exception.getCode();
        if (code == 408 || code == 429 || code == 0 || code >= 500) {
            return KycErrorClassification.RETRYABLE;
        }
        if (code >= 400 && code < 500) {
            return KycErrorClassification.NON_RETRYABLE;
        }
        return KycErrorClassification.RETRYABLE;
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new KycClientCommunicationException(
                    KycErrorClassification.RETRYABLE,
                    "KYC retry backoff interrupted",
                    interruptedException
            );
        }
    }
}
