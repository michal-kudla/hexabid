package com.acme.auctions.payment.api;

import com.acme.auctions.core.auctioning.model.Price;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Optional;

/**
 * Unified interface for all payment gateways.
 * This is the "Domain Client" mentioned in the requirements.
 */
public interface PaymentGateway {

    /**
     * Initiates a payment for a specific auction.
     *
     * @param request The payment request details.
     * @return The response containing the redirect URL or payment status.
     */
    PaymentResponse initiatePayment(PaymentRequest request);

    /**
     * Gets the unique identifier of the payment gateway.
     */
    String gatewayId();

    record PaymentRequest(
            String transactionId,
            Price amount,
            String description,
            URI callbackUrl
    ) {}

    record PaymentResponse(
            PaymentStatus status,
            Optional<URI> redirectUrl,
            @Nullable String gatewayTransactionId,
            @Nullable String errorMessage
    ) {}

    enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
}
