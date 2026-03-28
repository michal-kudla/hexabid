package com.acme.auctions.payment.adapter.p24;

import com.acme.auctions.payment.api.PaymentGateway;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public class Przelewy24Adapter implements PaymentGateway {

    @Override
    public PaymentResponse initiatePayment(PaymentRequest request) {
        // Mocking Przelewy24 REST API call
        System.out.println("Initiating Przelewy24 payment for " + request.amount());
        
        return new PaymentResponse(
                PaymentStatus.PENDING,
                Optional.of(URI.create("https://sandbox.przelewy24.pl/trnDirect/" + UUID.randomUUID())),
                UUID.randomUUID().toString(),
                null
        );
    }

    @Override
    public String gatewayId() {
        return "P24";
    }
}
