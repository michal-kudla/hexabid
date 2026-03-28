package com.acme.auctions.payment.adapter.local;

import com.acme.auctions.payment.api.PaymentGateway;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

/**
 * Local mock payment gateway for development purposes.
 * Supports multiple currencies and provides a link to a local simulation page.
 */
public class LocalPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResponse initiatePayment(PaymentRequest request) {
        System.out.println("Initiating Local Mock Payment for " + request.amount() + " (" + request.amount().currency() + ")");
        
        // Mocking a successful initiation with a link to a local simulation
        return new PaymentResponse(
                PaymentStatus.PENDING,
                Optional.of(URI.create("http://localhost:8080/dev-payment-mock?txId=" + UUID.randomUUID())),
                "LOCAL-" + UUID.randomUUID(),
                null
        );
    }

    @Override
    public String gatewayId() {
        return "LOCAL";
    }
}
