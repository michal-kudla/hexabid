package com.github.hexabid.payment.adapter.crypto;

import com.github.hexabid.payment.api.PaymentGateway;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

/**
 * Complex implementation for Cryptocurrency payments via Coinbase Commerce.
 */
public class CoinbaseCommerceAdapter implements PaymentGateway {

    @Override
    public PaymentResponse initiatePayment(PaymentRequest request) {
        // Mocking Coinbase Commerce API call
        // This is "complex" because it involves multiple crypto assets, 
        // blockchain confirmation delays, and price volatility handling.
        System.out.println("Initiating Crypto payment for " + request.amount());
        
        return new PaymentResponse(
                PaymentStatus.PENDING,
                Optional.of(URI.create("https://commerce.coinbase.com/charges/" + UUID.randomUUID())),
                UUID.randomUUID().toString(),
                null
        );
    }

    @Override
    public String gatewayId() {
        return "CRYPTO_COINBASE";
    }
}
